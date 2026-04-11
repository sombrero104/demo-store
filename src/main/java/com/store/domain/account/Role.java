package com.store.domain.account;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "role",
        uniqueConstraints = @UniqueConstraint(name = "uk_role_code", columnNames = "code")
)
public class Role {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String code; // 예: ORDER_READ, ORDER_WRITE, PRODUCT_ADMIN

    @Column(nullable = false, length = 100)
    private String name; // 화면용

    @Column(nullable = false)
    private boolean enabled = true;

    private Instant createdDate = Instant.now();
    private Instant updatedDate = Instant.now();

    @PreUpdate
    void preUpdate() { this.updatedDate = Instant.now(); }

    protected Role() {}

    public Role(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public boolean isEnabled() { return enabled; }

}

