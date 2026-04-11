package com.store.auth;

import com.store.domain.account.Account;
import com.store.domain.account.AccountRole;
import com.store.domain.account.Role;
import com.store.domain.account.RoleGroup;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class AccountAdapter extends User {

    private final transient Account account;

    public AccountAdapter(Account account) {
        super(account.getEmail(), account.getPassword(), authorities(account.getRoleGroups()));
        this.account = account;
    }

    private static Collection<? extends GrantedAuthority> authorities(Set<RoleGroup> roleGroups) {
        return roleGroups.stream()
                .filter(RoleGroup::isEnabled)
                .flatMap(g -> g.getRoles().stream())
                .filter(Role::isEnabled)
                .map(Role::getCode)
                .distinct()
                .map(SimpleGrantedAuthority::new) // hasAuthority('ORDER_READ') 와 매칭
                .collect(Collectors.toSet());
    }

    /*private static Collection<? extends GrantedAuthority> authorities(Set<AccountRole> roles) {
        return roles.stream()
                .map(accountRole -> new SimpleGrantedAuthority(accountRole.getRoleName()))
                .collect(Collectors.toSet());
    }*/

    @Override
    public boolean equals(Object other) {
        return super.equals(other);
    }

    @Override
    public int hashCode() {
        return this.account.getEmail().hashCode();
    }

}
