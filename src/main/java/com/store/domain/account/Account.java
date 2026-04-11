package com.store.domain.account;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "account")
@Getter
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 150)
    private String email;

    @Column(nullable = false, length = 200)
    @Setter
    private String password;

    @Column(length = 100)
    private String nickname;

    @ManyToMany
    @JoinTable(
            name = "account_role_group",
            joinColumns = @JoinColumn(name = "account_id"),
            inverseJoinColumns = @JoinColumn(name = "role_group_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_account_role_group",
                    columnNames = {"account_id", "role_group_id"}
            )
    )
    private Set<RoleGroup> roleGroups = new HashSet<>();

    @Column(nullable = false)
    private boolean enabled = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    private LocalDateTime modifiedDate;

    protected Account() {
    }

    @Builder
    private Account(String email, String password, String nickname, boolean enabled) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.enabled = enabled;
    }

    public static Account ofSignUp(String email, String encodedPassword, String nickname) {
        return Account.builder()
                .email(email)
                .password(encodedPassword)
                .nickname(nickname)
                .enabled(true)
                .build();
    }

    public void addRoleGroup(RoleGroup group) {
        if (Objects.isNull(this.roleGroups)) {
            this.roleGroups = new HashSet<>();
        }
        this.roleGroups.add(group);
    }

}
