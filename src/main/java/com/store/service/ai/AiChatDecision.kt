package com.store.service.ai

import com.store.dto.ai.AiChatIntent

data class AiChatDecision(
    val intent: AiChatIntent? = null,
    val reply: String? = null,
)
