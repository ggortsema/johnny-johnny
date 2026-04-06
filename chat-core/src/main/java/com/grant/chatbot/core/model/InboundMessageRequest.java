package com.grant.chatbot.core.model;

public record InboundMessageRequest(
        String conversationId,
        String senderId,
        String senderType,
        String messageText,
        int hourOfDay
) {
}
