package com.store.service;

import com.store.domain.account.Account;
import com.store.domain.orders.OrderItemRepository;
import com.store.domain.orders.OrderRepository;
import com.store.domain.orders.Orders;
import com.store.domain.product.Product;
import com.store.domain.product.ProductColor;
import com.store.domain.product.ProductOption;
import com.store.domain.product.ProductOptionRepository;
import com.store.dto.MessageResponseDto;
import com.store.dto.OrderItemRequestDto;
import com.store.dto.OrderRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class OrderServiceRetryTest {

    @Autowired
    private OrderService orderService;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private ProductOptionRepository productOptionRepository;

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private OrderItemRepository orderItemRepository;

    @BeforeEach
    void setUp() {
        Account account = Account.ofSignUp("user@store.com", "encoded", "user");
        ProductOption option = ProductOption.of(
                ProductColor.of(Product.of("셔츠", List.of()), "검정", List.of(), List.of()),
                "L",
                1000,
                10
        );

        when(accountService.getAccountByEmail(anyString())).thenReturn(account);
        when(productOptionRepository.findById(anyLong())).thenReturn(Optional.of(option));
        when(orderRepository.save(any(Orders.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("낙관적 락 예외가 발생하면 최대 3회까지 재시도 후 성공한다")
    void placeOrderRetriesAndSucceeds() {
        // 처음 주문 2번은 orderRepository.save()에서 강제로 실패시킴.
        AtomicInteger saveCallCount = new AtomicInteger(0);
        when(orderRepository.save(any(Orders.class))).thenAnswer(invocation -> {
            int attempt = saveCallCount.incrementAndGet();
            if (attempt < 3) {
                throw new OptimisticLockingFailureException("optimistic lock conflict");
            }
            return invocation.getArgument(0);
        });

        // 실제 주문 요청은 1번.
        // orderService.placeOrder()의 @Retryable에 설정된 'maxAttempts = 3'으로 인해
        // 자동으로 3번 실행되는지 확인한다.
        MessageResponseDto response = orderService.placeOrder(
                "user@store.com",
                new OrderRequestDto(List.of(new OrderItemRequestDto(1L, 1)))
        );

        // Assert
        assertEquals("주문 완료.", response.getMessage());
        verify(orderRepository, times(3)).save(any(Orders.class)); // 3번 실행되었는지 확인.
    }

    @Test
    @DisplayName("낙관적 락 예외가 계속 발생하면 3회 시도 후 예외를 던진다")
    void placeOrderRetriesAndFails() {
        OrderRequestDto request = new OrderRequestDto(List.of(new OrderItemRequestDto(1L, 1)));

        // orderRepository.save()에서 강제로 실패시킴.
        when(orderRepository.save(any(Orders.class)))
                .thenThrow(new OptimisticLockingFailureException("optimistic lock conflict"));

        // Assert
        OptimisticLockingFailureException ex = assertThrows(
                OptimisticLockingFailureException.class,
                () -> orderService.placeOrder("user@store.com", request)
        );
        assertEquals("optimistic lock conflict", ex.getMessage());
        verify(orderRepository, times(3)).save(any(Orders.class)); // 3번 실행되었는지 확인.
    }

}
