package com.store.service;

import com.store.domain.account.Account;
import com.store.domain.orders.OrderItem;
import com.store.domain.orders.OrderItemRepository;
import com.store.domain.orders.OrderRepository;
import com.store.domain.orders.Orders;
import com.store.dto.MessageResponseDto;
import com.store.dto.OrderRequestDto;
import com.store.dto.OrderResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private static final String INVALID_ORDER_ITEM_ID = "Invalid order item ID: ";

    private final AccountService accountService;
    private final OrderPlacementExecutor orderPlacementExecutor;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional(readOnly = true, timeout = 3)
    public List<OrderResponseDto> getOrderList(String currentUserEmail, int page, int size) {
        Account account = accountService.getAccountByEmail(currentUserEmail);
        // 1. 먼저 주문 아이디만 페이징 조회.
        // (이유: 컬렉션(fetch join)까지 한 번에 페이지 조회하면 중복/성능 문제가 쉽게 생김.)
        List<Long> orderIds = orderRepository.findOrderIdsByAccountId(account.getId(), PageRequest.of(page, size));
        if (orderIds.isEmpty()) {
            return List.of();
        }

        // 2. 그 다음 필요한 연관 데이터(주문상품/옵션/색상/이미지)를 한 번에 조회.
        // 이렇게 하면 주문 개수만큼 추가 쿼리가 반복되는 N+1 문제를 줄일 수 있다.
        List<Orders> orders = orderRepository.findDetailOrdersByIds(orderIds);

        return orders.stream()
                .map(OrderResponseDto::from)
                .toList();
    }

    public MessageResponseDto placeOrder(String currentUserEmail, OrderRequestDto orderRequestDto) {
        Account account = accountService.getAccountByEmail(currentUserEmail);
        orderPlacementExecutor.place(account, orderRequestDto.getItems());

        return MessageResponseDto.of("주문 완료.");
    }

    @Transactional(timeout = 10)
    public MessageResponseDto cancelOrderItems(String currentUserEmail, List<Long> orderItemIds) {
        Account account = accountService.getAccountByEmail(currentUserEmail);
        for (Long itemId : orderItemIds) {
            OrderItem orderItem = findOrderItem(itemId);
            Orders order = orderItem.getOrders();
            order.validateOwner(account.getId(), itemId);
            order.cancelOrderItem(orderItem);
        }

        return MessageResponseDto.of("주문 상품 취소 완료.");
    }

    private OrderItem findOrderItem(Long orderItemId) {
        return orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new IllegalArgumentException(INVALID_ORDER_ITEM_ID + orderItemId));
    }

}
