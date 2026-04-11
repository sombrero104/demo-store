package com.store.domain.account;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface JwtRepository extends CrudRepository<JwtToken, Long> {

    Optional<JwtToken> findByAccountId(Long accountId);

    long deleteByRefreshToken(String refreshToken);
}
