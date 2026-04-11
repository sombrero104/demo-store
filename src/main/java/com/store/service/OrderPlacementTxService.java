package com.store.service;

import com.store.domain.account.Account;
import com.store.domain.orders.OrderRepository;
import com.store.domain.orders.Orders;
import com.store.domain.product.ProductOption;
import com.store.domain.product.ProductOptionRepository;
import com.store.dto.OrderItemRequestDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderPlacementTxService {
    private static final String INVALID_PRODUCT_OPTION_ID = "Invalid product option ID: ";
    private static final Logger log = LoggerFactory.getLogger(OrderPlacementTxService.class);

    private final ProductOptionRepository productOptionRepository;
    private final OrderRepository orderRepository;

    @Transactional(timeout = 10)
    public void placeOrder(Account account, List<OrderItemRequestDto> items) {
        Orders order = Orders.of(account);
        for (OrderItemRequestDto item : items) {
            ProductOption option = findProductOption(item.getProductOptionId());
            option.decreaseStock(item.getQuantity());
            order.addOrderItem(option, item.getQuantity(), option.getPrice());
        }
        orderRepository.save(order);
        log.debug("[Order][Success] accountEmail={}, orderId={}, optionIds={}",
                account.getEmail(),
                order.getId(),
                items.stream().map(OrderItemRequestDto::getProductOptionId).toList());
    }

    private ProductOption findProductOption(Long productOptionId) {
        return productOptionRepository.findById(productOptionId)
                .orElseThrow(() -> new IllegalArgumentException(INVALID_PRODUCT_OPTION_ID + productOptionId));
    }
}
