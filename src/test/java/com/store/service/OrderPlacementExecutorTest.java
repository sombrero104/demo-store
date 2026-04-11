package com.store.service;

import com.store.domain.account.Account;
import com.store.dto.OrderItemRequestDto;
import com.store.exception.OrderLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderPlacementExecutorTest {

    @Mock
    private OrderPlacementTxService orderPlacementTxService;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock orderLock;

    private OrderPlacementExecutor executor;

    @BeforeEach
    void setUp() {
        executor = new OrderPlacementExecutor(redissonClient, orderPlacementTxService);
    }

    @Test
    @DisplayName("락 획득 후 주문 저장이 완료되면 락을 해제한다")
    void placeOrderWithLock() throws InterruptedException {
        Account account = Account.ofSignUp("user@store.com", "encoded", "user");
        List<OrderItemRequestDto> items = List.of(new OrderItemRequestDto(1L, 2));

        given(redissonClient.getLock(anyString())).willReturn(orderLock);
        given(orderLock.tryLock(eq(3L), eq(TimeUnit.SECONDS))).willReturn(true);
        given(orderLock.isHeldByCurrentThread()).willReturn(true);

        executor.place(account, items);

        verify(orderPlacementTxService).placeOrder(account, items);
        verify(orderLock).unlock();
    }

    @Test
    @DisplayName("락 획득에 실패하면 주문 저장을 중단한다")
    void failWhenLockAcquisitionFails() throws InterruptedException {
        Account account = Account.ofSignUp("user@store.com", "encoded", "user");
        List<OrderItemRequestDto> items = List.of(new OrderItemRequestDto(1L, 1));

        given(redissonClient.getLock(anyString())).willReturn(orderLock);
        given(orderLock.tryLock(eq(3L), eq(TimeUnit.SECONDS))).willReturn(false);

        OrderLockException ex = assertThrows(
                OrderLockException.class,
                () -> executor.place(account, items)
        );

        assertEquals("주문 처리 중 락 획득 실패: optionId=1", ex.getMessage());
        verify(orderPlacementTxService, never()).placeOrder(account, items);
    }
}
