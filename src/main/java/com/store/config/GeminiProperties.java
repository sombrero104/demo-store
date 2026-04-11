package com.store.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.ai.gemini")
@Data
public class GeminiProperties {

    private boolean enabled;

    private String apiKey;

    private String model;

    private String baseUrl;

    private Long timeoutSeconds;

}
