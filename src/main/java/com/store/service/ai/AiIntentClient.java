package com.store.service.ai;

public interface AiIntentClient {

    AiChatDecision decide(String sanitizedMessage);

}
