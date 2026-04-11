package com.store.domain.account;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "role_group",
        uniqueConstraints = @UniqueConstraint(name = "uk_role_group_code", columnNames = "code")
)
public class RoleGroup {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 예: ORDER_MANAGER, CS_AGENT ...
    @Column(nullable = false, length = 100)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private boolean enabled = true;

    private Instant createdDate = Instant.now();
    private Instant updatedDate = Instant.now();

    @ManyToMany
    @JoinTable(
            name = "role_group_role",
            joinColumns = @JoinColumn(name = "role_group_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_role_group_role",
                    columnNames = {"role_group_id", "role_id"}
            )
    )
    private Set<Role> roles = new HashSet<>();

    @PreUpdate
    void preUpdate() { this.updatedDate = Instant.now(); }

    protected RoleGroup() {
    }

    public RoleGroup(String code, String name, boolean enabled) {
        this.code = code;
        this.name = name;
        this.enabled = enabled;
    }

    public Long getId() { return id; }

    public String getCode() { return code; }

    public boolean isEnabled() { return enabled; }

    public Set<Role> getRoles() { return roles; }

}

