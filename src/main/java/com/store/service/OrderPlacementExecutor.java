package com.store.service;

import com.store.domain.account.Account;
import com.store.dto.OrderItemRequestDto;
import com.store.exception.OrderLockException;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class OrderPlacementExecutor {
    private static final long LOCK_WAIT_SECONDS = 3L;
    private static final String PRODUCT_OPTION_LOCK_PREFIX = "lock:product-option:";
    private static final Logger log = LoggerFactory.getLogger(OrderPlacementExecutor.class);

    private final RedissonClient redissonClient;
    private final OrderPlacementTxService orderPlacementTxService;

    public void place(Account account, List<OrderItemRequestDto> items) {
        List<RLock> locks = acquireLocks(items);
        try {
            orderPlacementTxService.placeOrder(account, items);
        } catch (RuntimeException ex) {
            log.debug("[Redisson][Result] Order failed for accountEmail={}, optionIds={}, cause={}",
                    account.getEmail(),
                    items.stream().map(OrderItemRequestDto::getProductOptionId).toList(),
                    ex.getClass().getSimpleName());
            throw ex;
        } finally {
            releaseLocks(locks);
        }
    }

    private List<RLock> acquireLocks(List<OrderItemRequestDto> items) {
        return items.stream()
                .map(OrderItemRequestDto::getProductOptionId)
                .distinct()
                .sorted()
                .map(this::acquireLock)
                .toList();
    }

    private RLock acquireLock(Long productOptionId) {
        log.debug("[Redisson][1] Trying Redisson lock for productOptionId={}", productOptionId);
        RLock lock = redissonClient.getLock(PRODUCT_OPTION_LOCK_PREFIX + productOptionId);
        try {
            boolean locked = lock.tryLock(LOCK_WAIT_SECONDS, TimeUnit.SECONDS);
            if (!locked) {
                log.debug("[Redisson][Exception] Failed to acquire Redisson lock for productOptionId={}", productOptionId);
                throw new OrderLockException(productOptionId);
            }
            log.debug("[Redisson][2] Acquired Redisson lock for productOptionId={}", productOptionId);
            return lock;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.debug("[Redisson][Exception] Interrupted while acquiring Redisson lock for productOptionId={}", productOptionId, ex);
            throw new OrderLockException(productOptionId, ex);
        }
    }

    private void releaseLocks(List<RLock> locks) {
        for (int i = locks.size() - 1; i >= 0; i--) {
            RLock lock = locks.get(i);
            if (lock.isHeldByCurrentThread()) {
                log.debug("[Redisson][3] Releasing Redisson lock name={}", lock.getName());
                lock.unlock();
            }
        }
    }
}
