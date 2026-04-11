package com.store.service.ai;

import com.store.dto.ai.AiChatIntent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiChatDecision {

    private AiChatIntent intent;

    private String reply;

}
