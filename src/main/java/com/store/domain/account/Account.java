package com.store.domain.account;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "account")
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

    /*@ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(value = EnumType.STRING)
    private Set<AccountRole> roles;*/

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

    /*public static Account of(JoinRequestDto joinRequestDto) {
        Account account = new Account();
        account.email = joinRequestDto.getEmail();
        account.password = joinRequestDto.getPassword();
        account.nickname = joinRequestDto.getNickname();
        // account.roles = joinRequestDto.getRoles();
        return account;
    }

    public static Account of(String email, String password, String nickname, Set<AccountRole> roles) {
        Account account = new Account();
        account.email = email;
        account.password = password;
        account.nickname = nickname;
        // account.roles = roles;
        return account;
    }*/

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

    public Long getId() { return id; }

    public String getEmail() { return email; }

    public String getPassword() { return password; }

    public boolean isEnabled() { return enabled; }

    public Set<RoleGroup> getRoleGroups() { return roleGroups; }

    public void addRoleGroup(RoleGroup group) {
        if (Objects.isNull(this.roleGroups)) {
            this.roleGroups = new HashSet<>();
        }
        this.roleGroups.add(group);
    }

}
