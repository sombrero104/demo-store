package com.store.domain.orders;

import com.store.domain.product.ProductOption;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Entity
@EqualsAndHashCode(of = "id")
@Getter
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "orders_id", nullable = false)
    private Orders orders;

    @ManyToOne
    @JoinColumn(name = "product_option_id", nullable = false)
    private ProductOption option;

    private int quantity;

    private int price;

    @Enumerated(EnumType.STRING)
    private OrderItemStatus status;

    public static OrderItem of(Orders orders, ProductOption option, int quantity, int price) {
        OrderItem item = new OrderItem();
        item.orders = orders;
        item.option = option;
        item.quantity = quantity;
        item.price = price;
        item.status = OrderItemStatus.ORDERED;
        return item;
    }

    public boolean isCanceled() {
        return (OrderItemStatus.CANCELED == this.status);
    }

    public boolean canCancel() {
        return this.status == OrderItemStatus.ORDERED
                || this.status == OrderItemStatus.PREPARING;
    }

    public void cancel() {
        this.status = OrderItemStatus.CANCELED;
    }

    public boolean cancelAndRestoreStock() {
        if (!canCancel()) {
            return false;
        }
        this.option.increaseStock(this.quantity);
        cancel();
        return true;
    }

    void assignToOrder(Orders orders) {
        this.orders = orders;
    }

}
