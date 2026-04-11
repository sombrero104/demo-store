package com.store.domain.account;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface JwtRepository extends CrudRepository<JwtToken, Long> {

    Optional<JwtToken> findByRefreshToken(String refreshToken);
}
