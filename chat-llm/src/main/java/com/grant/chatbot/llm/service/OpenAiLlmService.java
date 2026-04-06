package com.grant.chatbot.llm.service;

import com.grant.chatbot.core.model.InboundMessageRequest;
import com.grant.chatbot.core.model.PersonaContext;
import com.grant.chatbot.core.model.ReplyCandidate;
import com.grant.chatbot.core.model.StyleExample;
import com.grant.chatbot.core.service.LlmService;
import com.grant.chatbot.core.service.PolicyDecision;
import com.grant.chatbot.llm.model.OpenAiReplySchema;
import com.openai.client.OpenAIClient;
import com.openai.core.JsonSchemaLocalValidation;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.StructuredChatCompletion;
import com.openai.models.chat.completions.StructuredChatCompletionCreateParams;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenAiLlmService implements LlmService {
    private final OpenAIClient client;
    private final ChatModel model;
    private final int maxCompletionTokens;
    
    private static final Logger logger = LoggerFactory.getLogger(OpenAiLlmService.class);

    public OpenAiLlmService(OpenAIClient client, ChatModel model, int maxCompletionTokens) {
        this.client = Objects.requireNonNull(client, "client");
        this.model = Objects.requireNonNull(model, "model");
        this.maxCompletionTokens = maxCompletionTokens;
    }

    @Override
    public List<ReplyCandidate> generateCandidates(InboundMessageRequest request, PersonaContext context, PolicyDecision decision) {
        try {
            StructuredChatCompletionCreateParams<OpenAiReplySchema> params = ChatCompletionCreateParams.builder()
                    .model(model)
                    .maxCompletionTokens(maxCompletionTokens)
                    .addSystemMessage(buildSystemPrompt())
                    .addUserMessage(buildUserPrompt(request, context, decision))
                    .responseFormat(OpenAiReplySchema.class, JsonSchemaLocalValidation.NO)
                    .build();

            StructuredChatCompletion<OpenAiReplySchema> completion = client.chat()
                    .completions()
                    .create(params);

            List<ReplyCandidate> parsed = completion.choices().stream()
                    .flatMap(choice -> choice.message().content().stream())
                    .flatMap(schema -> schema.candidates.stream())
                    .filter(candidate -> candidate != null && candidate.text != null && !candidate.text.isBlank())
                    .map(candidate -> new ReplyCandidate(
                            normalizeVariant(candidate.variant),
                            candidate.text.trim(),
                            clamp(candidate.styleScore, 0.0, 1.0, 0.90),
                            clamp(candidate.riskScore, 0.0, 1.0, heuristicRisk(candidate.text, request.messageText())),
                            false,
                            candidate.note == null || candidate.note.isBlank() ? "openai" : candidate.note.trim()))
                    .sorted(Comparator.comparingInt(reply -> variantPriority(reply.variant())))
                    .collect(Collectors.toCollection(ArrayList::new));

            return ensureThreeCandidates(parsed, request, context, decision);
        } catch (Exception ex) {
        	logger.info("IN FALLBACK CANDIDATE ERROR CONDITION");
        	logger.error("ERROR IN GENERATE CANDIDATES", ex);
            return fallbackCandidates(request, context, decision, ex.getClass().getSimpleName());
        }
    }

    private String buildSystemPrompt() {
        return "You write text-message replies that sound like the user, not like an AI assistant. "
               + "Match the user's tone, pacing, and directness. Avoid corporate filler, disclaimers, or meta commentary. "
               + "Return exactly three candidates in structured JSON: concise, natural, and bold. "
               + "Each candidate must be realistic for a text message and must not mention being an AI.";
    }

    private String buildUserPrompt(InboundMessageRequest request, PersonaContext context, PolicyDecision decision) {
        String examples = context.styleExamples() == null || context.styleExamples().isEmpty()
                ? "(none)"
                : context.styleExamples().stream()
                    .limit(5)
                    .map(this::formatExample)
                    .collect(Collectors.joining("\n"));

        return "Create reply suggestions for the following inbound message.\n\n"
               + "Inbound message:\n" + request.messageText() + "\n\n"
               + "Sender type: " + safe(request.senderType()) + "\n"
               + "Conversation id: " + safe(request.conversationId()) + "\n"
               + "Hour of day: " + request.hourOfDay() + "\n\n"
               + "Persona context:\n"
               + "- Base tone: " + safe(context.baseTone()) + "\n"
               + "- Relationship summary: " + safe(context.relationshipSummary()) + "\n"
               + "- Avoid phrases: " + joinList(context.avoidPhrases()) + "\n\n"
               + "Representative style examples:\n" + examples + "\n\n"
               + "Rule constraints:\n"
               + "- Persona variant: " + safe(decision.getPersonaVariant()) + "\n"
               + "- Humor allowed: " + decision.isHumorAllowed() + "\n"
               + "- Approval required: " + decision.isApprovalRequired() + "\n"
               + "- Max words per reply: " + decision.getMaxWords() + "\n\n"
               + "Output requirements:\n"
               + "- concise: shortest useful answer\n"
               + "- natural: most likely real reply from the user\n"
               + "- bold: a slightly stronger version, still believable\n"
               + "- Keep every reply under the max words limit\n"
               + "- Keep punctuation and wording natural for texting\n"
               + "- styleScore and riskScore must each be between 0.0 and 1.0\n";
    }

    private String formatExample(StyleExample example) {
        return "- Inbound: " + safe(example.inboundText()) + "\n"
               + "  Actual reply: " + safe(example.actualReply()) + "\n"
               + "  Tags: " + safe(example.tags());
    }

    private List<ReplyCandidate> ensureThreeCandidates(List<ReplyCandidate> parsed,
                                                       InboundMessageRequest request,
                                                       PersonaContext context,
                                                       PolicyDecision decision) {
        List<ReplyCandidate> result = new ArrayList<>(parsed);
        
        logger.info("response from openai: " + parsed.stream());

        if (result.stream().noneMatch(candidate -> "concise".equals(candidate.variant()))) {
            result.add(defaultCandidate("concise", request, context, decision));
        }
        if (result.stream().noneMatch(candidate -> "natural".equals(candidate.variant()))) {
            result.add(defaultCandidate("natural", request, context, decision));
        }
        if (result.stream().noneMatch(candidate -> "bold".equals(candidate.variant()))) {
            result.add(defaultCandidate("bold", request, context, decision));
        }

        return result.stream()
                .collect(Collectors.toMap(
                        ReplyCandidate::variant,
                        candidate -> candidate,
                        (left, right) -> left))
                .values()
                .stream()
                .sorted(Comparator.comparingInt(reply -> variantPriority(reply.variant())))
                .limit(3)
                .toList();
    }

    private ReplyCandidate defaultCandidate(String variant,
                                            InboundMessageRequest request,
                                            PersonaContext context,
                                            PolicyDecision decision) {
        String incoming = request.messageText() == null ? "" : request.messageText().trim();
        String tone = context.baseTone() == null ? "direct" : context.baseTone();

        String text;
        if (incoming.endsWith("?")) {
            text = switch (variant) {
                case "concise" -> "Yeah, that works for me.";
                case "bold" -> decision.isHumorAllowed()
                        ? "Yeah, that works. Let's not overcomplicate it."
                        : "Yes, that works. Let's do that.";
                default -> tone.toLowerCase(Locale.ROOT).contains("warm")
                        ? "Yeah, that should work for me. Let's do that."
                        : "That works for me. Let's go with it.";
            };
        } else {
            text = switch (variant) {
                case "concise" -> "Sounds good.";
                case "bold" -> decision.isHumorAllowed()
                        ? "Sounds good. Let's just move on it."
                        : "Sounds good. Let's move forward.";
                default -> "Sounds good to me.";
            };
        }

        return new ReplyCandidate(
                variant,
                trimToMaxWords(text, decision.getMaxWords()),
                0.82,
                heuristicRisk(text, incoming),
                false,
                "fallback");
    }

    private List<ReplyCandidate> fallbackCandidates(InboundMessageRequest request,
                                                    PersonaContext context,
                                                    PolicyDecision decision,
                                                    String failureLabel) {
        return List.of(
                defaultCandidate("concise", request, context, decision),
                defaultCandidate("natural", request, context, decision),
                new ReplyCandidate(
                        "bold",
                        trimToMaxWords(defaultCandidate("bold", request, context, decision).text(), decision.getMaxWords()),
                        0.80,
                        Math.min(1.0, defaultCandidate("bold", request, context, decision).riskScore() + 0.05),
                        false,
                        "fallback-" + failureLabel.toLowerCase(Locale.ROOT))
        );
    }

    private String normalizeVariant(String variant) {
        if (variant == null) {
            return "natural";
        }
        String normalized = variant.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "concise", "natural", "bold" -> normalized;
            default -> "natural";
        };
    }

    private int variantPriority(String variant) {
        return switch (normalizeVariant(variant)) {
            case "concise" -> 0;
            case "natural" -> 1;
            case "bold" -> 2;
            default -> 3;
        };
    }

    private double clamp(double value, double min, double max, double fallback) {
        if (Double.isNaN(value) || Double.isInfinite(value) || value == 0.0) {
            return fallback;
        }
        return Math.max(min, Math.min(max, value));
    }

    private double heuristicRisk(String replyText, String inboundText) {
        String combined = (safe(replyText) + " " + safe(inboundText)).toLowerCase(Locale.ROOT);
        double risk = 0.15;
        if (combined.contains("money") || combined.contains("lawyer") || combined.contains("hospital")) {
            risk += 0.45;
        }
        if (combined.contains("angry") || combined.contains("upset") || combined.contains("mad")) {
            risk += 0.25;
        }
        if (replyText != null && replyText.length() > 180) {
            risk += 0.10;
        }
        return Math.min(1.0, risk);
    }

    private String trimToMaxWords(String text, int maxWords) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String[] words = text.trim().split("\\s+");
        if (words.length <= maxWords) {
            return text.trim();
        }
        return String.join(" ", java.util.Arrays.copyOf(words, maxWords));
    }

    private String joinList(List<String> items) {
        if (items == null || items.isEmpty()) {
            return "(none)";
        }
        return items.stream().filter(Objects::nonNull).collect(Collectors.joining(", "));
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "(none)" : value;
    }
}
