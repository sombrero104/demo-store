package com.store.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.ai.openai")
data class OpenAiProperties(
    val isEnabled: Boolean = false,
    val apiKey: String = "",
    val model: String = "",
    val baseUrl: String = "",
    val timeoutSeconds: Long = 10,
)
