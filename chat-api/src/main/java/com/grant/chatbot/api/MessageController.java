package com.grant.chatbot.api;

import com.grant.chatbot.core.model.InboundMessageRequest;
import com.grant.chatbot.core.model.ReplySuggestionResponse;
import com.grant.chatbot.core.service.ChatOrchestrator;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(originPatterns = "http://*:*")
public class MessageController {
    private final ChatOrchestrator chatOrchestrator;

    public MessageController(ChatOrchestrator chatOrchestrator) {
        this.chatOrchestrator = chatOrchestrator;
    }

    @PostMapping("/suggest")
    public ReplySuggestionResponse suggest(@Valid @RequestBody InboundMessageRequest request) {
        return chatOrchestrator.suggestReplies(request);
    }
}
