package com.grant.chatbot.core.model;

public record ReplyCandidate(
        String variant,
        String text,
        double styleScore,
        double riskScore,
        boolean approvedByRules,
        String ruleNotes
) {
}
