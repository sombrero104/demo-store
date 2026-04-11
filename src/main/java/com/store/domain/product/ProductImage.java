package com.store.domain.product;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Entity
@EqualsAndHashCode(of = "id")
@Getter
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_color_id", nullable = false)
    private ProductColor productColor;

    @NotNull
    @Column(nullable = false)
    private String imageUrl;

    private boolean isMain;

}
