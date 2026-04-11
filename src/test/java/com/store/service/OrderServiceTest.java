package com.store.service;

import com.store.domain.account.Account;
import com.store.domain.account.RoleGroup;
import com.store.domain.orders.OrderItem;
import com.store.domain.orders.OrderItemRepository;
import com.store.domain.orders.OrderRepository;
import com.store.domain.orders.Orders;
import com.store.domain.product.Product;
import com.store.domain.product.ProductColor;
import com.store.domain.product.ProductOption;
import com.store.dto.MessageResponseDto;
import com.store.dto.OrderItemRequestDto;
import com.store.dto.OrderRequestDto;
import com.store.dto.OrderResponseDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private AccountService accountService;

    @Mock
    private OrderPlacementExecutor orderPlacementExecutor;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    private static Account account;

    private static final String EMAIL = "user@store.com";

    @BeforeAll
    static void before() {
        account = Account.builder()
                .email(EMAIL)
                .password("1234")
                .nickname("user")
                .enabled(true)
                .build();
        account.addRoleGroup(new RoleGroup("USER", "USER", true));
        ReflectionTestUtils.setField(account, "id", 1L);
    }

    @Test
    @DisplayName("주문리스트 조회")
    void getOrderList() {
        // Given
        List<Orders> orders = new ArrayList<>();
        orders.add(Orders.of(account));
        List<Long> orderIds = List.of(10L);

        when(accountService.getAccountByEmail(anyString())).thenReturn(account);
        when(orderRepository.findOrderIdsByAccountId(account.getId(), PageRequest.of(0, 5))).thenReturn(orderIds);
        when(orderRepository.findDetailOrdersByIds(orderIds)).thenReturn(orders);

        // When
        List<OrderResponseDto> orderResponseDtos = orderService.getOrderList(EMAIL, 0, 5);

        // Then
        assertEquals(1L, orderResponseDtos.get(0).getAccountId());
        assertNotNull(orderResponseDtos.get(0).getItemList());
    }

    @Test
    @DisplayName("주문 내역이 없으면 빈 리스트 반환")
    void getOrderListWhenNoOrders() {
        when(accountService.getAccountByEmail(anyString())).thenReturn(account);
        when(orderRepository.findOrderIdsByAccountId(account.getId(), PageRequest.of(0, 5))).thenReturn(List.of());

        List<OrderResponseDto> orderResponseDtos = orderService.getOrderList(EMAIL, 0, 5);

        assertTrue(orderResponseDtos.isEmpty());
        verify(orderRepository, never()).findDetailOrdersByIds(anyList());
    }

    @Test
    @DisplayName("주문하기")
    void placeOrder() {
        // Given
        List<OrderItemRequestDto> items = new ArrayList<>();
        items.add(new OrderItemRequestDto(1L, 1));
        when(accountService.getAccountByEmail(anyString())).thenReturn(account);

        // When
        MessageResponseDto messageResponseDto = orderService.placeOrder(EMAIL, new OrderRequestDto(items));

        // Then
        assertEquals("주문 완료.", messageResponseDto.getMessage());
    }

    @Test
    @DisplayName("주문 취소")
    void cancelOrderItems() {
        // Given
        ProductColor productColor = ProductColor.of(Product.of("셔츠", new ArrayList<>()), "검정", new ArrayList<>(), new ArrayList<>());
        ProductOption option = ProductOption.of(productColor, "L", 1000, 1);
        OrderItem item = OrderItem.of(Orders.of(account), option, 1, 1000);

        when(accountService.getAccountByEmail(anyString())).thenReturn(account);
        when(orderItemRepository.findById(1L)).thenReturn(Optional.of(item));

        // When
        MessageResponseDto messageResponseDto = orderService.cancelOrderItems(EMAIL, List.of(1L));

        // Then
        assertEquals("주문 상품 취소 완료.", messageResponseDto.getMessage());
    }

}
