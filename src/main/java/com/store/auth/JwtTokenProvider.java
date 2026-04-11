package com.store.auth;

import com.store.domain.account.Account;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {
    
    private final JwtProperties jwtProperties;

    public String generateAccessToken(Account account) {
        return Jwts.builder()
                .subject(account.getEmail())
                .issuedAt(new Date())
                .expiration(getAccessExpiration())
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(Account account) {
        return Jwts.builder()
                .subject(account.getEmail())
                .issuedAt(new Date())
                .expiration(getRefreshExpiration())
                .signWith(getSigningKey())
                .compact();
    }

    public String getEmail(String token) {
        Claims claims = parseClaims(token);
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        parseClaims(token);
        return true;
    }

    public long getAccessExpirationSeconds() {
        return jwtProperties.getAccessExpirationTime() / 1000L;
    }

    public long getRefreshExpirationSeconds() {
        return jwtProperties.getRefreshExpirationTime() / 1000L;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token).getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Date getAccessExpiration() {
        return new Date(System.currentTimeMillis() + jwtProperties.getAccessExpirationTime());
    }

    private Date getRefreshExpiration() {
        return new Date(System.currentTimeMillis() + jwtProperties.getRefreshExpirationTime());
    }

}
