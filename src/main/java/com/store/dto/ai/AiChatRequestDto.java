package com.store.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AiChatRequestDto {

    @Schema(description = "사용자 메시지", example = "내 주문 내역 보여줘")
    @NotBlank(message = "Message cannot be empty.")
    @Size(max = 1000, message = "Message must be 1000 characters or less.")
    private String message;

}
