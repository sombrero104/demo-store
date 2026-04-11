package com.store.domain.product;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Entity
@EqualsAndHashCode(of = "id")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductColor> colors = new ArrayList<>();

    public static Product of(String name, List<ProductColor> colors) {
        Product product = new Product();
        product.name = name;
        product.colors = colors;
        return product;
    }

}
