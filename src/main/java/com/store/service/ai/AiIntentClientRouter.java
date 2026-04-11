package com.store.service.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class AiIntentClientRouter implements AiIntentClient {

    private final Map<AiProvider, AiIntentClient> clientMap;
    private final AiProvider provider;

    public AiIntentClientRouter(
            OpenAiChatCompletionsClient openAiClient,
            GeminiChatClient geminiClient,
            @Value("${app.ai.provider:}") AiProvider provider
    ) {
        this.provider = provider;
        this.clientMap = Map.of(
                AiProvider.OPENAI, openAiClient,
                AiProvider.GEMINI, geminiClient
        );
    }

    @Override
    public AiChatDecision decide(String message) {
        return clientMap.get(provider).decide(message);
    }

}