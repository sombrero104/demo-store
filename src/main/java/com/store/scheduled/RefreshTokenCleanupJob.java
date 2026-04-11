package com.store.scheduled;

import com.store.domain.account.JwtRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenCleanupJob {

    private final JwtRepository jwtRepository;

    // 매일 새벽 3시. (원하는 주기로 조정.)
    /*@Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupExpiredRefreshTokens() {
        long deleted = jwtRepository.deleteByExpirationTimeBefore(Instant.now());
        log.info("Deleted expired refresh tokens: {}", deleted);
    }*/

}

