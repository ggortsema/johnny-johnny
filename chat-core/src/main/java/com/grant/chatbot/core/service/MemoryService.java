package com.grant.chatbot.core.service;

import com.grant.chatbot.core.model.InboundMessageRequest;
import com.grant.chatbot.core.model.PersonaContext;

public interface MemoryService {
    PersonaContext buildPersonaContext(InboundMessageRequest request);
}
