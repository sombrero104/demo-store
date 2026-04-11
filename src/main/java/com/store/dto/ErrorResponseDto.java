package com.store.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponseDto {

    private String code;

    private String message;

    private Instant timestamp;

    private String path;

    private String traceId;

    public static ErrorResponseDto of(String code, String message, String path, String traceId) {
        return ErrorResponseDto.builder()
                .code(code)
                .message(message)
                .timestamp(Instant.now())
                .path(path)
                .traceId(traceId)
                .build();
    }

}
