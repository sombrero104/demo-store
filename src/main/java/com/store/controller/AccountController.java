package com.store.controller;

import com.store.config.SecurityCookieProperties;
import com.store.dto.JoinRequestDto;
import com.store.dto.LoginRequestDto;
import com.store.dto.MessageResponseDto;
import com.store.dto.TokenResponseDto;
import com.store.dto.auth.TokenPair;
import com.store.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/account")
@RestController
@RequiredArgsConstructor
public class AccountController {

    private static final String REFRESH_COOKIE_NAME = "refreshToken";

    private final AccountService accountService;
    private final SecurityCookieProperties securityCookieProperties;

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        TokenPair pair = accountService.login(loginRequestDto);
        return tokenResponse(pair);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDto> refresh(
            @CookieValue(name = REFRESH_COOKIE_NAME, required = false) String refreshToken
    ) {
        if (refreshToken == null) {
            throw new BadCredentialsException("Refresh token is required.");
        }
        TokenPair pair = accountService.refresh(refreshToken);
        return tokenResponse(pair);
    }

    @PostMapping("/join")
    public ResponseEntity<MessageResponseDto> join(@Valid @RequestBody JoinRequestDto joinRequestDto) {
        MessageResponseDto commonResponseDto = accountService.join(joinRequestDto);
        return ResponseEntity.ok(commonResponseDto);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken
    ) {
        if (refreshToken != null) {
            accountService.logout(refreshToken); // DB revoke/delete
        }

        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(securityCookieProperties.isSecure())
                .sameSite(securityCookieProperties.getSameSite())
                .path(securityCookieProperties.getPath())
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .build();
    }

    private ResponseEntity<TokenResponseDto> tokenResponse(TokenPair pair) {
        ResponseCookie cookie = buildRefreshCookie(pair.getRefreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new TokenResponseDto(pair.getAccessToken(), pair.getExpiresIn()));
    }

    private ResponseCookie buildRefreshCookie(String refreshToken) {
        return ResponseCookie.from(REFRESH_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(securityCookieProperties.isSecure())
                .sameSite(securityCookieProperties.getSameSite())
                .path(securityCookieProperties.getPath())
                .maxAge(securityCookieProperties.getMaxAgeSeconds())
                .build();
    }

}
