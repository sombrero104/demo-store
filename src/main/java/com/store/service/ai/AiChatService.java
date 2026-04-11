package com.store.service.ai;

import com.store.dto.OrderItemResponseDto;
import com.store.dto.OrderResponseDto;
import com.store.dto.ai.AiChatIntent;
import com.store.dto.ai.AiChatRequestDto;
import com.store.dto.ai.AiChatResponseDto;
import com.store.dto.ai.OrderActionDto;
import com.store.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.store.dto.ai.AiChatIntent.ORDER_CANCEL;
import static com.store.dto.ai.AiChatIntent.ORDER_LIST;

@Service
@RequiredArgsConstructor
public class AiChatService {

    private static final int DEFAULT_ORDER_PAGE = 0;
    private static final int DEFAULT_ORDER_SIZE = 5;

    private final OrderService orderService;
    private final AiIntentClientRouter aiIntentClientRouter;
    private final AiPromptSanitizer aiPromptSanitizer;

    public AiChatResponseDto chat(String currentUserEmail, AiChatRequestDto requestDto) {
        String sanitizedMessage = aiPromptSanitizer.sanitize(requestDto.getMessage());
        AiChatDecision decision = aiIntentClientRouter.decide(sanitizedMessage);
        AiChatIntent intent = decision.getIntent();
        return switch (intent) {
            case ORDER_LIST -> answerOrderList(currentUserEmail);
            case ORDER_CANCEL -> answerOrderCancel(currentUserEmail);
            case ACCOUNT_HELP, GENERAL_HELP, UNKNOWN -> AiChatResponseDto.of(intent, decision.getReply());
        };
    }

    private AiChatResponseDto answerOrderCancel(String currentUserEmail) {
        List<OrderResponseDto> orders = orderService.getOrderList(currentUserEmail, DEFAULT_ORDER_PAGE, DEFAULT_ORDER_SIZE);

        if (orders.isEmpty()) {
            return AiChatResponseDto.of(ORDER_CANCEL, "취소 가능한 주문이 없습니다.");
        }

        List<OrderActionDto> actions = orders.stream()
                .map(o -> new OrderActionDto(
                        o.getOrderId(),
                        o.getOrderId() + "번 주문 취소"
                ))
                .toList();

        return AiChatResponseDto.of(ORDER_CANCEL, "취소할 주문을 선택해주세요.", actions);
    }

    private AiChatResponseDto answerOrderList(String currentUserEmail) {
        List<OrderResponseDto> orders = orderService.getOrderList(currentUserEmail, DEFAULT_ORDER_PAGE, DEFAULT_ORDER_SIZE);

        if (orders.isEmpty()) {
            return AiChatResponseDto.of(ORDER_LIST, "최근 주문 내역이 없습니다.");
        }

        List<OrderActionDto> actions = orders.stream()
                .map(o -> new OrderActionDto(
                        o.getOrderId(), o.getOrderId() + "번 (" + o.getStatus() + ")"
                ))
                .toList();

        return AiChatResponseDto.of(ORDER_LIST, "최근 주문 " + orders.size() + "건이 있습니다.", actions);
    }

    private String summarizeOrder(OrderResponseDto order) {
        String itemSummary = order.getItemList().stream()
                .map(this::summarizeItem)
                .collect(Collectors.joining(", "));

        return "주문 " + order.getOrderId() + "는 " + order.getStatus() + " 상태이며 주문내역은 [" + itemSummary + "] 입니다.";
    }

    private String summarizeItem(OrderItemResponseDto item) {
        return item.getQuantity() + "개(" + item.getStatus() + ")";
    }

}
