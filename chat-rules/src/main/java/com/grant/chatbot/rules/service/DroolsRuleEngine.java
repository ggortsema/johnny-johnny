package com.grant.chatbot.rules.service;

import com.grant.chatbot.core.model.InboundMessageRequest;
import com.grant.chatbot.core.model.ReplyCandidate;
import com.grant.chatbot.core.service.PolicyDecision;
import com.grant.chatbot.core.service.RuleEngine;
import com.grant.chatbot.rules.config.DroolsConfig;
import com.grant.chatbot.rules.facts.CandidateReplyFact;
import com.grant.chatbot.rules.facts.InboundMessageFact;
import com.grant.chatbot.rules.facts.PersonaDecisionFact;

import org.kie.api.runtime.KieSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class DroolsRuleEngine implements RuleEngine {

    @Override
    public PolicyDecision evaluateInbound(InboundMessageRequest request) {
        InboundMessageFact inbound = new InboundMessageFact();
        inbound.setSenderType(request.senderType());
        inbound.setText(request.messageText());
        inbound.setHourOfDay(request.hourOfDay());
        inbound.setContainsSensitiveTopic(containsSensitiveTopic(request.messageText()));
        inbound.setContainsConflictSignals(containsConflictSignals(request.messageText()));

        PersonaDecisionFact decisionFact = new PersonaDecisionFact();

        KieSession session = DroolsConfig.kieContainer().newKieSession("chatKSession");
        try {
            session.insert(inbound);
            session.insert(decisionFact);
            session.fireAllRules();
        } finally {
            session.dispose();
        }

        PolicyDecision decision = new PolicyDecision();
        decision.setPersonaVariant(decisionFact.getPersonaVariant());
        decision.setHumorAllowed(decisionFact.isHumorAllowed());
        decision.setAutoSendAllowed(decisionFact.isAutoSendAllowed());
        decision.setApprovalRequired(decisionFact.isApprovalRequired());
        decision.setMaxWords(decisionFact.getMaxWords());
        return decision;
    }

    @Override
    public List<ReplyCandidate> evaluateCandidates(List<ReplyCandidate> candidates, PolicyDecision decision) {
        KieSession session = DroolsConfig.kieContainer().newKieSession("chatKSession");
        try {
            PersonaDecisionFact decisionFact = new PersonaDecisionFact();
            decisionFact.setPersonaVariant(decision.getPersonaVariant());
            decisionFact.setHumorAllowed(decision.isHumorAllowed());
            decisionFact.setAutoSendAllowed(decision.isAutoSendAllowed());
            decisionFact.setApprovalRequired(decision.isApprovalRequired());
            decisionFact.setMaxWords(decision.getMaxWords());
            session.insert(decisionFact);

            List<CandidateReplyFact> facts = new ArrayList<>();
            for (ReplyCandidate candidate : candidates) {
                CandidateReplyFact fact = new CandidateReplyFact();
                fact.setVariant(candidate.variant());
                fact.setText(candidate.text());
                fact.setWordCount(wordCount(candidate.text()));
                fact.setStyleScore(candidate.styleScore());
                fact.setRiskScore(candidate.riskScore());
                session.insert(fact);
                facts.add(fact);
            }
            session.fireAllRules();

            return facts.stream()
                    .filter(f -> !f.isRejected())
                    .map(f -> new ReplyCandidate(
                            f.getVariant(),
                            f.getText(),
                            f.getStyleScore(),
                            f.getRiskScore(),
                            true,
                            f.getRejectionReason() == null ? "accepted" : f.getRejectionReason()))
                    .collect(Collectors.toList());
        } finally {
            session.dispose();
        }
    }

    private boolean containsSensitiveTopic(String text) {
        String normalized = text.toLowerCase(Locale.ROOT);
        return normalized.contains("lawyer") || normalized.contains("hospital") || normalized.contains("money");
    }

    private boolean containsConflictSignals(String text) {
        String normalized = text.toLowerCase(Locale.ROOT);
        return normalized.contains("angry") || normalized.contains("upset") || normalized.contains("mad") || normalized.contains("why did you");
    }

    private int wordCount(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        return text.trim().split("\\s+").length;
    }
}
