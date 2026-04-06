package com.grant.chatbot.core.service;

import java.util.List;

import com.grant.chatbot.core.model.InboundMessageRequest;
import com.grant.chatbot.core.model.PersonaContext;
import com.grant.chatbot.core.model.ReplyCandidate;

public interface LlmService {
    List<ReplyCandidate> generateCandidates(InboundMessageRequest request, PersonaContext context, PolicyDecision decision);
}
