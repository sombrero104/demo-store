package com.store.domain.account;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

@RedisHash("refresh-token")
@Getter
public class JwtToken {

    @Id
    private Long accountId;

    @Indexed
    private String refreshToken;

    @TimeToLive
    private Long ttlSeconds;

    public static JwtToken of(Long accountId, String refreshToken, long ttlSeconds) {
        JwtToken jwtToken = new JwtToken();
        jwtToken.accountId = accountId;
        jwtToken.refreshToken = refreshToken;
        jwtToken.ttlSeconds = ttlSeconds;
        return jwtToken;
    }

    public void updateRefreshToken(String refreshToken, long ttlSeconds) {
        this.refreshToken = refreshToken;
        this.ttlSeconds = ttlSeconds;
    }
}
