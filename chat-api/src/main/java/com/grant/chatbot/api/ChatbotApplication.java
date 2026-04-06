package com.grant.chatbot.api;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.grant.chatbot.core.service.ChatOrchestrator;
import com.grant.chatbot.core.service.LlmService;
import com.grant.chatbot.core.service.MemoryService;
import com.grant.chatbot.core.service.RuleEngine;
import com.grant.chatbot.llm.service.OpenAiLlmService;
import com.grant.chatbot.llm.service.StubLlmService;
import com.grant.chatbot.memory.service.InMemoryMemoryService;
import com.grant.chatbot.rules.service.DroolsRuleEngine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = "com.grant.chatbot")
public class ChatbotApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatbotApplication.class, args);
    }

    @Bean
    MemoryService memoryService() {
        return new InMemoryMemoryService();
    }

    @Bean
    RuleEngine ruleEngine() {
        return new DroolsRuleEngine();
    }

    @Bean
    LlmService llmService(
            @Value("${app.llm.provider:openai}") String provider,
            @Value("${app.llm.openai.model:GPT_5_4}") String configuredModel,
            @Value("${app.llm.openai.max-completion-tokens:450}") int maxCompletionTokens
    ) {
        if (!"openai".equalsIgnoreCase(provider)) {
            return new StubLlmService();
        }

        OpenAIClient client = OpenAIOkHttpClient.builder()
                .fromEnv()
                .build();

        return new OpenAiLlmService(client, resolveModel(configuredModel), maxCompletionTokens);
    }

    @Bean
    ChatOrchestrator chatOrchestrator(MemoryService memoryService, RuleEngine ruleEngine, LlmService llmService) {
        return new ChatOrchestrator(memoryService, ruleEngine, llmService);
    }

    private ChatModel resolveModel(String configuredModel) {
        if (configuredModel == null) {
            return ChatModel.CHATGPT_4O_LATEST;
        }
        return switch (configuredModel.trim().toUpperCase()) {
            case "GPT_5_2" -> ChatModel.GPT_4O;
            case "GPT_5_4" -> ChatModel.GPT_4O;
            default -> ChatModel.GPT_4O;
        };
    }
}
