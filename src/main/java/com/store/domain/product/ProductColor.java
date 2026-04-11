package com.store.domain.product;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Entity
@EqualsAndHashCode(of = "id")
@Getter
public class ProductColor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull
    @Column(nullable = false, length = 30)
    private String color;

    @OneToMany(mappedBy = "productColor", cascade = CascadeType.ALL)
    @BatchSize(size = 100)
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "productColor", cascade = CascadeType.ALL)
    private List<ProductOption> options = new ArrayList<>();

    public static ProductColor of(Product product, String color, List<ProductImage> images, List<ProductOption> options) {
        ProductColor productColor = new ProductColor();
        productColor.product = product;
        productColor.color = color;
        productColor.images = images;
        productColor.options = options;
        return productColor;
    }

}
