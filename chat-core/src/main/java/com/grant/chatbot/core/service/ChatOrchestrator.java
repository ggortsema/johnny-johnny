package com.grant.chatbot.core.service;

import java.util.List;

import com.grant.chatbot.core.model.InboundMessageRequest;
import com.grant.chatbot.core.model.PersonaContext;
import com.grant.chatbot.core.model.ReplyCandidate;
import com.grant.chatbot.core.model.ReplySuggestionResponse;

public class ChatOrchestrator {
    private final MemoryService memoryService;
    private final RuleEngine ruleEngine;
    private final LlmService llmService;

    public ChatOrchestrator(MemoryService memoryService, RuleEngine ruleEngine, LlmService llmService) {
        this.memoryService = memoryService;
        this.ruleEngine = ruleEngine;
        this.llmService = llmService;
    }

    public ReplySuggestionResponse suggestReplies(InboundMessageRequest request) {
        PolicyDecision decision = ruleEngine.evaluateInbound(request);
        PersonaContext context = memoryService.buildPersonaContext(request);
        List<ReplyCandidate> rawCandidates = llmService.generateCandidates(request, context, decision);
        List<ReplyCandidate> filteredCandidates = ruleEngine.evaluateCandidates(rawCandidates, decision);

        return new ReplySuggestionResponse(
                decision.getPersonaVariant(),
                decision.isApprovalRequired(),
                filteredCandidates
        );
    }
}
