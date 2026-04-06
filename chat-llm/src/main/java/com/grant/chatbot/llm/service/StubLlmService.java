package com.grant.chatbot.llm.service;

import com.grant.chatbot.core.model.InboundMessageRequest;
import com.grant.chatbot.core.model.PersonaContext;
import com.grant.chatbot.core.model.ReplyCandidate;
import com.grant.chatbot.core.service.LlmService;
import com.grant.chatbot.core.service.PolicyDecision;

import java.util.List;

public class StubLlmService implements LlmService {
    @Override
    public List<ReplyCandidate> generateCandidates(InboundMessageRequest request, PersonaContext context, PolicyDecision decision) {
        String incoming = request.messageText();
        return List.of(
                new ReplyCandidate("concise", "Yep, that works. Let's do it.", 0.91, 0.10, false, "pre-rules"),
                new ReplyCandidate("natural", "That works for me. I’d keep it simple and just move ahead.", 0.94, 0.18, false, "pre-rules"),
                new ReplyCandidate("bold", "Yeah, let's stop overthinking it and just ship the first pass.", 0.88, incoming.contains("angry") ? 0.82 : 0.26, false, "pre-rules")
        );
    }
}
