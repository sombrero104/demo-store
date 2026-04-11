package com.store.service.ai;

import com.store.dto.OrderResponseDto;
import com.store.dto.ai.AiChatIntent;
import com.store.dto.ai.AiChatRequestDto;
import com.store.dto.ai.AiChatResponseDto;
import com.store.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiChatServiceTest {

    private AiChatService aiChatService;

    @Mock
    private OrderService orderService;

    @Mock
    private AiIntentClientRouter aiIntentClientRouter;

    private final AiPromptSanitizer aiPromptSanitizer = new AiPromptSanitizer();

    @BeforeEach
    void setUp() {
        aiChatService = new AiChatService(orderService, aiIntentClientRouter, aiPromptSanitizer);
    }

    @Test
    @DisplayName("주문 조회 의도면 주문 서비스 결과를 요약한다")
    void chatOrderList() {
        OrderResponseDto order = OrderResponseDto.builder()
                .orderId(10L)
                .status(com.store.domain.orders.OrderStatus.ORDERED)
                .itemList(List.of())
                .build();

        when(orderService.getOrderList(eq("user@store.com"), eq(0), eq(5)))
                .thenReturn(List.of(order));
        when(aiIntentClientRouter.decide(eq("내 주문 내역 보여줘")))
                .thenReturn(AiChatDecision.builder()
                        .intent(AiChatIntent.ORDER_LIST)
                        .reply("주문을 확인해드릴게요.")
                        .build());

        AiChatResponseDto response = aiChatService.chat(
                "user@store.com",
                new AiChatRequestDto("내 주문 내역 보여줘")
        );

        assertEquals(AiChatIntent.ORDER_LIST, response.getIntent());
        assertTrue(response.getAnswer().contains("최근 주문 1건"));
        assertNotNull(response.getActions());
        assertEquals(1, response.getActions().size());
        assertEquals("10번 (ORDERED)", response.getActions().get(0).getLabel());
        verify(orderService).getOrderList("user@store.com", 0, 5);
    }

    @Test
    @DisplayName("알 수 없는 메시지는 UNKNOWN 의도로 응답한다")
    void chatUnknown() {
        when(aiIntentClientRouter.decide(eq("오늘 날씨 어때")))
                .thenReturn(AiChatDecision.builder()
                        .intent(AiChatIntent.UNKNOWN)
                        .reply("지원하지 않는 요청입니다.")
                        .build());

        AiChatResponseDto response = aiChatService.chat(
                "user@store.com",
                new AiChatRequestDto("오늘 날씨 어때")
        );

        assertEquals(AiChatIntent.UNKNOWN, response.getIntent());
        assertTrue(response.getAnswer().contains("지원하지 않는 요청"));
    }
}
