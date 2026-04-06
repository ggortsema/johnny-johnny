package com.grant.chatbot.core.model;

import java.util.List;

public record ReplySuggestionResponse(
        String personaVariant,
        boolean approvalRequired,
        List<ReplyCandidate> candidates
) {
}
