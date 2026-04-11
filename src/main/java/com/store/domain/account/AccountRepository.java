package com.store.domain.account;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    @EntityGraph(attributePaths = {
            "roleGroups",
            "roleGroups.roles"
    })
    Optional<Account> findByEmail(String email);

}