package com.grant.chatbot.memory.service;

import com.grant.chatbot.core.model.InboundMessageRequest;
import com.grant.chatbot.core.model.PersonaContext;
import com.grant.chatbot.core.model.StyleExample;
import com.grant.chatbot.core.service.MemoryService;

import java.util.List;

public class InMemoryMemoryService implements MemoryService {
    @Override
    public PersonaContext buildPersonaContext(InboundMessageRequest request) {
        return new PersonaContext(
                "direct, warm, concise",
                List.of("synergy", "circle back", "As an AI"),
                List.of(
                        new StyleExample("Can you make it tomorrow?", "Yeah, tomorrow works for me.", "brief, friendly"),
                        new StyleExample("What do you think?", "I think we should keep it simple and ship the first version.", "direct, practical")
                ),
                "Known contact with light preference for short replies"
        );
    }
}
