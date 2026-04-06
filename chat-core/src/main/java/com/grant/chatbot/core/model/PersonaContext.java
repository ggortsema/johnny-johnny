package com.grant.chatbot.core.model;

import java.util.List;

public record PersonaContext(
        String baseTone,
        List<String> avoidPhrases,
        List<StyleExample> styleExamples,
        String relationshipSummary
) {
}
