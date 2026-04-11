package com.store.auth;

import com.store.domain.account.Account;
import com.store.domain.account.JwtRepository;
import com.store.domain.account.JwtToken;
import com.store.dto.auth.TokenPair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtRepository jwtRepository;

    @Transactional(timeout = 10)
    public TokenPair issueTokens(Account account) {
        String accessToken = jwtTokenProvider.generateAccessToken(account);
        String refreshToken = jwtTokenProvider.generateRefreshToken(account);
        long expiresIn = jwtTokenProvider.getAccessExpirationSeconds();
        long refreshTtlSeconds = jwtTokenProvider.getRefreshExpirationSeconds();

        // 계정당 refresh token 1개만 유지하도록 Redis에 업서트한다.
        JwtToken jwtToken = jwtRepository.findById(account.getId())
                .map(saved -> {
                    saved.updateRefreshToken(refreshToken, refreshTtlSeconds);
                    return saved;
                })
                .orElseGet(() -> JwtToken.of(account.getId(), refreshToken, refreshTtlSeconds));
        jwtRepository.save(jwtToken);

        return new TokenPair(accessToken, refreshToken, expiresIn);
    }

    @Transactional(timeout = 10)
    public TokenPair rotateTokens(Account account, JwtToken savedToken) {
        String newAccessToken = jwtTokenProvider.generateAccessToken(account);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(account);
        long expiresIn = jwtTokenProvider.getAccessExpirationSeconds();
        long refreshTtlSeconds = jwtTokenProvider.getRefreshExpirationSeconds();

        // Redis에 저장된 refresh 토큰을 새 값으로 교체한다. (로테이션)
        savedToken.updateRefreshToken(newRefreshToken, refreshTtlSeconds);
        jwtRepository.save(savedToken);

        return new TokenPair(newAccessToken, newRefreshToken, expiresIn);
    }

}
