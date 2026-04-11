package com.store.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiChatResponseDto {

    private AiChatIntent intent;

    private String answer;

    private List<OrderActionDto> actions;

    public static AiChatResponseDto of(AiChatIntent intent, String answer) {
        return AiChatResponseDto.builder()
                .intent(intent)
                .answer(answer)
                .build();
    }

    public static AiChatResponseDto of(AiChatIntent intent, String answer, List<OrderActionDto> actions) {
        return AiChatResponseDto.builder()
                .intent(intent)
                .answer(answer)
                .actions(actions)
                .build();
    }

}
