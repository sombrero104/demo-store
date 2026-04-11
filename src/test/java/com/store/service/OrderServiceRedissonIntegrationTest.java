package com.store.service;

import com.store.domain.account.Account;
import com.store.domain.account.AccountRepository;
import com.store.domain.account.RoleGroup;
import com.store.domain.account.RoleGroupRepository;
import com.store.domain.orders.OrderRepository;
import com.store.domain.product.ProductOption;
import com.store.domain.product.ProductOptionRepository;
import com.store.dto.OrderItemRequestDto;
import com.store.dto.OrderRequestDto;
import com.store.exception.OutOfStockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@EnabledIf(expression = "#{T(com.store.test.RedisTestSupport).isRedisAvailable()}", loadContext = false)
@Sql(scripts = "/sql/test-reset.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class OrderServiceRedissonIntegrationTest {

    private static final int THREAD_COUNT = 10;
    private static final Long PRODUCT_OPTION_ID = 2L;

    @Autowired
    private OrderService orderService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RoleGroupRepository roleGroupRepository;

    @Autowired
    private ProductOptionRepository productOptionRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RedissonClient redissonClient;

    private String currentUserEmail;

    @BeforeEach
    void setUp() {
        redissonClient.getKeys().flushdb();

        long suffix = System.nanoTime();
        currentUserEmail = "redisson-user-" + suffix + "@store.com";

        RoleGroup userGroup = roleGroupRepository.findByCode("USER")
                .orElseThrow(() -> new IllegalStateException("USER role group not found"));

        Account account = Account.ofSignUp(currentUserEmail, "encoded", "user");
        account.addRoleGroup(userGroup);
        accountRepository.save(account);
    }

    @Test
    @DisplayName("같은 상품 옵션으로 동시 주문이 들어와도 재고 수량만큼만 성공한다")
    void placeOrderConcurrentlyWithRedissonLock() throws Exception {
        ProductOption option = productOptionRepository.findById(PRODUCT_OPTION_ID)
                .orElseThrow(() -> new IllegalStateException("Product option not found."));
        int initialStock = option.getStock();

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch readyLatch = new CountDownLatch(THREAD_COUNT);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT);
        AtomicInteger successCount = new AtomicInteger();
        ConcurrentLinkedQueue<Throwable> failures = new ConcurrentLinkedQueue<>();
        OrderRequestDto request = new OrderRequestDto(List.of(new OrderItemRequestDto(PRODUCT_OPTION_ID, 1)));

        try {
            for (int i = 0; i < THREAD_COUNT; i++) {
                executorService.submit(() -> {
                    readyLatch.countDown();
                    try {
                        startLatch.await();
                        orderService.placeOrder(currentUserEmail, request);
                        successCount.incrementAndGet();
                    } catch (Throwable ex) {
                        failures.add(ex);
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            assertTrue(readyLatch.await(5, TimeUnit.SECONDS));
            startLatch.countDown();
            assertTrue(doneLatch.await(10, TimeUnit.SECONDS));
        } finally {
            executorService.shutdownNow();
        }

        ProductOption updatedOption = productOptionRepository.findById(PRODUCT_OPTION_ID)
                .orElseThrow(() -> new IllegalStateException("Updated product option not found."));

        assertEquals(initialStock, successCount.get());
        assertEquals(THREAD_COUNT - initialStock, failures.size());
        assertEquals(0, updatedOption.getStock());
        assertEquals(initialStock, orderRepository.count());
        assertTrue(allOutOfStock(failures));
    }

    private boolean allOutOfStock(ConcurrentLinkedQueue<Throwable> failures) {
        return failures.stream().allMatch(this::isOutOfStockFailure);
    }

    private boolean isOutOfStockFailure(Throwable throwable) {
        Throwable cause = throwable;
        while (cause != null) {
            if (cause instanceof OutOfStockException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

}
