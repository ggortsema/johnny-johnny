package com.grant.chatbot.core.service;

import java.util.List;

import com.grant.chatbot.core.model.InboundMessageRequest;
import com.grant.chatbot.core.model.ReplyCandidate;

public interface RuleEngine {
    PolicyDecision evaluateInbound(InboundMessageRequest request);
    List<ReplyCandidate> evaluateCandidates(List<ReplyCandidate> candidates, PolicyDecision decision);
}
