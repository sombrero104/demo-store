package com.store.service.ai

import com.store.domain.orders.OrderStatus
import com.store.dto.OrderResponseDto
import com.store.dto.ai.AiChatIntent
import com.store.dto.ai.AiChatRequestDto
import com.store.service.OrderService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue

@ExtendWith(MockitoExtension::class)
class AiChatServiceTest {

    private lateinit var aiChatService: AiChatService

    @Mock
    private lateinit var orderService: OrderService

    @Mock
    private lateinit var aiIntentClientRouter: AiIntentClientRouter

    private val aiPromptSanitizer = AiPromptSanitizer()

    @BeforeEach
    fun setUp() {
        aiChatService = AiChatService(orderService, aiIntentClientRouter, aiPromptSanitizer)
    }

    @Test
    @DisplayName("주문 조회 의도면 주문 서비스 결과를 요약한다")
    fun chatOrderList() {
        val order = OrderResponseDto(
            orderId = 10L,
            status = OrderStatus.ORDERED,
            itemList = emptyList(),
        )

        `when`(orderService.getOrderList(eq("user@store.com"), eq(0), eq(5)))
            .thenReturn(listOf(order))
        `when`(aiIntentClientRouter.decide(eq("내 주문 내역 보여줘")))
            .thenReturn(AiChatDecision(intent = AiChatIntent.ORDER_LIST, reply = "주문을 확인해드릴게요."))

        val response = aiChatService.chat(
            "user@store.com",
            AiChatRequestDto("내 주문 내역 보여줘"),
        )

        assertEquals(AiChatIntent.ORDER_LIST, response.getIntent())
        assertTrue(response.getAnswer().contains("최근 주문 1건"))
        assertNotNull(response.getActions())
        assertEquals(1, response.getActions().size)
        assertEquals("10번 (ORDERED)", response.getActions()[0].getLabel())
        verify(orderService).getOrderList("user@store.com", 0, 5)
    }

    @Test
    @DisplayName("알 수 없는 메시지는 UNKNOWN 의도로 응답한다")
    fun chatUnknown() {
        `when`(aiIntentClientRouter.decide(eq("오늘 날씨 어때")))
            .thenReturn(AiChatDecision(intent = AiChatIntent.UNKNOWN, reply = "지원하지 않는 요청입니다."))

        val response = aiChatService.chat(
            "user@store.com",
            AiChatRequestDto("오늘 날씨 어때"),
        )

        assertEquals(AiChatIntent.UNKNOWN, response.getIntent())
        assertTrue(response.getAnswer().contains("지원하지 않는 요청"))
    }
}
