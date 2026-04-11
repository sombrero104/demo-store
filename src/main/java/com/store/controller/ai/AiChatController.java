package com.store.controller.ai;

import com.store.auth.CurrentUserName;
import com.store.dto.ai.AiChatRequestDto;
import com.store.dto.ai.AiChatResponseDto;
import com.store.service.ai.AiChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class AiChatController {

    private final AiChatService aiChatService;

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponseDto> chat(
            @Valid @RequestBody AiChatRequestDto requestDto,
            @CurrentUserName String currentUserEmail
    ) {
        return ResponseEntity.ok(aiChatService.chat(currentUserEmail, requestDto));
    }

}
