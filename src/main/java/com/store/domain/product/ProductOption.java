package com.store.domain.product;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import com.store.exception.OutOfStockException;

@Entity
@EqualsAndHashCode(of = "id")
@Getter
public class ProductOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_color_id", nullable = false)
    private ProductColor productColor;

    @NotNull
    @Column(nullable = false, length = 20)
    private String size;

    @Column(nullable = false)
    private int price;

    private int stock;

    @Version
    private long version;

    public static ProductOption of(ProductColor productColor, String size, int price, int stock) {
        ProductOption option = new ProductOption();
        option.productColor = productColor;
        option.size = size;
        option.price = price;
        option.stock = stock;
        return option;
    }

    public void decreaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("차감 수량은 1 이상이어야 합니다.");
        }
        if (this.stock < quantity) {
            throw new OutOfStockException(this.id);
        }
        this.stock -= quantity;
    }

    public void increaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("복구 수량은 1 이상이어야 합니다.");
        }
        this.stock += quantity;
    }

}
