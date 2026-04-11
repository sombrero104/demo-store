package com.store.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.config.GeminiProperties;
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
public class GeminiChatClient implements AiIntentClient {

    private static final String SYSTEM_PROMPT = """
            You classify sanitized ecommerce support messages.
            The message never contains personal data and may contain placeholders like [EMAIL], [PHONE], [TOKEN], [NUMBER].
            Return JSON only with keys intent and reply.
            Allowed intent values: ORDER_LIST, ORDER_CANCEL, ACCOUNT_HELP, GENERAL_HELP, UNKNOWN.
            Reply must always be written in Korean.
            Keep reply concise and safe. Do not claim to have completed an action.
            """;

    private final GeminiProperties geminiProperties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public GeminiChatClient(
            GeminiProperties geminiProperties,
            ObjectMapper objectMapper,
            RestClient.Builder restClientBuilder
    ) {
        this.geminiProperties = geminiProperties;
        this.objectMapper = objectMapper;
        this.restClient = restClientBuilder
                .baseUrl(geminiProperties.getBaseUrl())
                .build();
    }

    @Retryable(
            value = {RestClientException.class, AiIntegrationException.class},
            maxAttempts = 5,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @Override
    public AiChatDecision decide(String sanitizedMessage) {
        validateConfiguration();

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", SYSTEM_PROMPT + "\n\nSanitized user message: " + sanitizedMessage)
                                )
                        )
                )
        );

        try {
            JsonNode response = restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1beta/models/" + geminiProperties.getModel() + ":generateContent")
                            .queryParam("key", geminiProperties.getApiKey())
                            .build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(JsonNode.class);

            return extractDecision(response);
        } catch (RestClientException ex) {
            log.error("[ai-chat] Gemini request failed", ex);
            throw new AiIntegrationException("AI service request failed.", ex);
        } catch (Exception ex) {
            log.error("[ai-chat] Failed to parse Gemini response", ex);
            throw new AiIntegrationException("AI service returned an invalid response.", ex);
        }
    }

    private void validateConfiguration() {
        if (!geminiProperties.isEnabled()) {
            throw new AiIntegrationException("AI service is disabled.");
        }
        if (!StringUtils.hasText(geminiProperties.getApiKey())) {
            throw new AiIntegrationException("Gemini API key is missing.");
        }
    }

    private AiChatDecision extractDecision(JsonNode response) throws Exception {
        JsonNode contentNode = response
                .path("candidates").path(0)
                .path("content").path("parts").path(0).path("text");

        if (!contentNode.isTextual()) {
            throw new AiIntegrationException("AI service returned empty content.");
        }

        String raw = contentNode.textValue();
        raw = raw
                .replace("```json", "")
                .replace("```", "")
                .replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "")
                .replace("\u0008", "")
                .trim();

        JsonNode decisionNode = objectMapper.readTree(raw);
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