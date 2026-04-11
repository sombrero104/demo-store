package com.store.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponseDto {

    private String message;

    public static MessageResponseDto of(String message) {
        return MessageResponseDto.builder()
                .message(message)
                .build();
    }

}
