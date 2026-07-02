package com.store.service;

import com.store.auth.JwtService;
import com.store.auth.JwtTokenProvider;
import com.store.domain.account.*;
import com.store.dto.JoinRequestDto;
import com.store.dto.LoginRequestDto;
import com.store.dto.MessageResponseDto;
import com.store.dto.auth.TokenPair;
import com.store.exception.AccountAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {
    private static final String DEFAULT_ROLE_GROUP_CODE = "USER";

    private final AccountRepository accountRepository;
    private final RoleGroupRepository roleGroupRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtRepository jwtRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional(timeout = 10)
    public TokenPair login(LoginRequestDto loginRequestDto) {
        Account account = getAccountByEmail(loginRequestDto.getUsername());

        if (!passwordEncoder.matches(loginRequestDto.getPassword(), account.getPassword())) {
           throw new BadCredentialsException("The password is incorrect.");
        }

        return jwtService.issueTokens(account);
    }

    @Transactional(timeout = 10)
    public TokenPair refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BadCredentialsException("Refresh token is not valid.");
        }

        String email = jwtTokenProvider.getEmail(refreshToken);
        Account account = getAccountByEmail(email);

        // Redis에 저장된 refresh 토큰 조회.
        JwtToken saved = jwtRepository.findById(account.getId())
                .orElseThrow(() -> new BadCredentialsException("Authentication is not valid."));

        // 저장된 토큰과 요청 토큰이 같은지 확인.
        if (!saved.getRefreshToken().equals(refreshToken)) {
            throw new BadCredentialsException("Refresh token is not valid.");
        }

        // 새 토큰 발급 후 Redis refresh 값을 갱신한다. (로테이션)
        return jwtService.rotateTokens(account, saved);
    }

    @Transactional(timeout = 10)
    public MessageResponseDto join(JoinRequestDto joinRequestDto) {
        if (accountRepository.findByEmail(joinRequestDto.getEmail()).isPresent()) {
            throw new AccountAlreadyExistsException(joinRequestDto.getEmail());
        }

        String encodedPassword = passwordEncoder.encode(joinRequestDto.getPassword());
        Account account = Account.ofSignUp(
                joinRequestDto.getEmail(),
                encodedPassword,
                joinRequestDto.getNickname()
        );
        RoleGroup defaultGroup = roleGroupRepository.findByCode(DEFAULT_ROLE_GROUP_CODE)
                .orElseThrow(() -> new IllegalStateException(
                        "Default role group not found: " + DEFAULT_ROLE_GROUP_CODE
                ));
        account.addRoleGroup(defaultGroup);

        Account saved = accountRepository.save(account);
        return MessageResponseDto.of("Successfully signed up: " + saved.getEmail());
    }

    @Transactional(timeout = 10)
    public void logout(String refreshToken) {
        jwtRepository.findByRefreshToken(refreshToken).ifPresentOrElse(
                savedToken -> {
                    jwtRepository.deleteById(savedToken.getAccountId());
                    log.info("[logout] refreshToken revoked/deleted.");
                },
                () -> log.warn("[logout] refreshToken not found in Redis.")
        );
    }

    @Transactional(readOnly = true, timeout = 3)
    public Account getAccountByEmail(String email) {
        return accountRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No account exists."));
    }

}
