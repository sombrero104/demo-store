package com.store.auth;

import com.store.domain.account.Account;
import com.store.domain.account.AccountRepository;
import com.store.domain.account.Role;
import com.store.domain.account.RoleGroup;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthorityService {

    private final AccountRepository accountRepository;

    /**
     * API 호출 시 해당 email의 현재 권한을 조회한다.
     */
    @Transactional(readOnly = true, timeout = 3)
    public List<SimpleGrantedAuthority> loadAuthorities(String email) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));

        return account.getRoleGroups().stream()
                .filter(RoleGroup::isEnabled)
                .flatMap(g -> g.getRoles().stream())
                .filter(Role::isEnabled)
                .map(Role::getCode) // 예: "PRODUCT_WRITE"
                .distinct()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

}
