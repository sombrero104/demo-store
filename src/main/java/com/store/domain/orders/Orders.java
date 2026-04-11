package com.store.domain.orders;

import com.store.domain.account.Account;
import com.store.domain.product.ProductOption;
import com.store.exception.OrderAccessDeniedException;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@EqualsAndHashCode(of = "id")
@Getter
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @OneToMany(mappedBy = "orders", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    public static Orders of(Account account) {
        Orders order = new Orders();
        order.account = account;
        order.status = OrderStatus.ORDERED;
        order.orderDate = LocalDateTime.now();
        return order;
    }

    public void addItem(OrderItem item) {
        this.orderItems.add(item);
        item.assignToOrder(this);
    }

    public void addOrderItem(ProductOption option, int quantity, int price) {
        OrderItem orderItem = OrderItem.of(this, option, quantity, price);
        addItem(orderItem);
    }

    public boolean isCanceled() {
        return (OrderStatus.CANCELED == this.status);
    }

    public void cancel() {
        this.status = OrderStatus.CANCELED;
    }

    public void cancelIfAllItemsCanceled() {
        boolean allCanceled = this.orderItems.stream().allMatch(OrderItem::isCanceled);
        if (allCanceled && !isCanceled()) {
            cancel();
        }
    }

    public void validateOwner(Long accountId, Long orderItemId) {
        if (!accountId.equals(this.account.getId())) {
            throw new OrderAccessDeniedException(orderItemId);
        }
    }

    public void cancelOrderItem(OrderItem orderItem) {
        boolean canceled = orderItem.cancelAndRestoreStock();
        if (canceled) {
            cancelIfAllItemsCanceled();
        }
    }

}
