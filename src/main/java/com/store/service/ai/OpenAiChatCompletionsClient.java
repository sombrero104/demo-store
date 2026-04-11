package com.store.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.config.OpenAiProperties;
import com.store.dto.ai.AiChatIntent;
import com.store.exception.AiIntegrationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class OpenAiChatCompletionsClient implements AiIntentClient {

    private static final String SYSTEM_PROMPT = """
            You classify sanitized ecommerce support messages.
            The message never contains personal data and may contain placeholders like [EMAIL], [PHONE], [TOKEN], [NUMBER].
            Return JSON only with keys intent and reply.
            Allowed intent values: ORDER_LIST, ORDER_CANCEL, ACCOUNT_HELP, GENERAL_HELP, UNKNOWN.
            Reply must always be written in Korean.
            Keep reply concise and safe. Do not claim to have completed an action.
            """;

    private final OpenAiProperties openAiProperties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public OpenAiChatCompletionsClient(
            OpenAiProperties openAiProperties,
            ObjectMapper objectMapper,
            RestClient.Builder restClientBuilder
    ) {
        this.openAiProperties = openAiProperties;
        this.objectMapper = objectMapper;
        this.restClient = restClientBuilder
                .baseUrl(openAiProperties.getBaseUrl())
                .build();
    }

    @Retryable(
            value = {RestClientException.class, AiIntegrationException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @Override
    public AiChatDecision decide(String sanitizedMessage) {
        validateConfiguration();

        Map<String, Object> requestBody = Map.of(
                "model", openAiProperties.getModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of("role", "user", "content", "Sanitized user message: " + sanitizedMessage)
                ),
                "response_format", Map.of("type", "json_object"),
                "temperature", 0.2
        );

        try {
            JsonNode response = restClient.post()
                    .uri("/v1/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + openAiProperties.getApiKey())
                    .body(requestBody)
                    .retrieve()
                    .body(JsonNode.class);

            return extractDecision(response);
        } catch (RestClientException ex) {
            log.error("[ai-chat] OpenAI request failed", ex);
            throw new AiIntegrationException("AI service request failed.", ex);
        } catch (Exception ex) {
            log.error("[ai-chat] Failed to parse OpenAI response", ex);
            throw new AiIntegrationException("AI service returned an invalid response.", ex);
        }
    }

    private void validateConfiguration() {
        if (!openAiProperties.isEnabled()) {
            throw new AiIntegrationException("AI service is disabled.");
        }
        if (!StringUtils.hasText(openAiProperties.getApiKey())) {
            throw new AiIntegrationException("OpenAI API key is missing.");
        }
    }

    private AiChatDecision extractDecision(JsonNode response) throws Exception {
        JsonNode contentNode = response.path("choices").path(0).path("message").path("content");
        if (!contentNode.isTextual()) {
            throw new AiIntegrationException("AI service returned empty content.");
        }

        JsonNode decisionNode = objectMapper.readTree(contentNode.asText());
        String intentValue = decisionNode.path("intent").asText("UNKNOWN");
        String reply = decisionNode.path("reply").asText("요청을 이해하지 못했습니다.");

        return AiChatDecision.builder()
                .intent(parseIntent(intentValue))
                .reply(reply)
                .build();
    }

    private AiChatIntent parseIntent(String intentValue) {
        try {
            return AiChatIntent.valueOf(intentValue);
        } catch (IllegalArgumentException ex) {
            return AiChatIntent.UNKNOWN;
        }
    }
}
