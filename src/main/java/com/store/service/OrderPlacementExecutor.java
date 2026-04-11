package com.store.service;

import com.store.domain.account.Account;
import com.store.domain.orders.OrderRepository;
import com.store.domain.orders.Orders;
import com.store.domain.product.ProductOption;
import com.store.domain.product.ProductOptionRepository;
import com.store.dto.OrderItemRequestDto;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderPlacementExecutor {
    private static final String INVALID_PRODUCT_OPTION_ID = "Invalid product option ID: ";

    private final ProductOptionRepository productOptionRepository;
    private final OrderRepository orderRepository;

    @Retryable(
            retryFor = {OptimisticLockException.class, OptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 200)
    )
    @Transactional(timeout = 10)
    public void place(Account account, List<OrderItemRequestDto> items) {
        Orders order = Orders.of(account);
        for (OrderItemRequestDto item : items) {
            ProductOption option = findProductOption(item.getProductOptionId());
            option.decreaseStock(item.getQuantity());
            order.addOrderItem(option, item.getQuantity(), option.getPrice());
        }
        orderRepository.save(order);
    }

    private ProductOption findProductOption(Long productOptionId) {
        return productOptionRepository.findById(productOptionId)
                .orElseThrow(() -> new IllegalArgumentException(INVALID_PRODUCT_OPTION_ID + productOptionId));
    }
}
