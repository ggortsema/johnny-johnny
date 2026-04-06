package com.grant.chatbot.llm.model;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.ArrayList;
import java.util.List;

@JsonClassDescription("Exactly three suggested replies for the user's voice clone assistant.")
public class OpenAiReplySchema {
    @JsonPropertyDescription("The three reply candidates. Include concise, natural, and bold variants in that order.")
    public List<OpenAiReplyVariant> candidates = new ArrayList<>();

    public static class OpenAiReplyVariant {
        @JsonPropertyDescription("Reply flavor. Use one of: concise, natural, bold.")
        public String variant;

        @JsonPropertyDescription("The reply text the user could plausibly send.")
        public String text;

        @JsonPropertyDescription("Estimated style match score from 0.0 to 1.0.")
        public double styleScore;

        @JsonPropertyDescription("Estimated risk score from 0.0 to 1.0.")
        public double riskScore;

        @JsonPropertyDescription("One short note about why this variant fits.")
        public String note;
    }
}
