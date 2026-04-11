package com.store.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.domain.account.*;
import com.store.dto.ErrorResponseDto;
import com.store.dto.JoinRequestDto;
import com.store.dto.LoginRequestDto;
import com.store.dto.TokenResponseDto;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/sql/test-reset.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class AccountControllerTest {
    private static final String DEFAULT_PASSWORD = "1234";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RoleGroupRepository roleGroupRepository;

    @Autowired
    private JwtRepository jwtRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String userEmail;

    @BeforeEach
    void setUpData() {
        long suffix = System.nanoTime();
        userEmail = "user" + suffix + "@store.com";

        RoleGroup userGroup = roleGroupRepository.findByCode("USER")
                .orElseThrow(() -> new IllegalStateException("USER role group not found"));

        Account user = Account.ofSignUp(userEmail, passwordEncoder.encode(DEFAULT_PASSWORD), "user");
        user.addRoleGroup(userGroup);
        accountRepository.save(user);
    }

    @Test
    void join() throws Exception {
        String email = "new-user" + System.nanoTime() + "@store.com";
        JoinRequestDto request = JoinRequestDto.builder()
                .email(email)
                .password(DEFAULT_PASSWORD)
                .nickname("new-user")
                .build();

        mockMvc.perform(post("/api/account/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully signed up: " + email));

        assertTrue(accountRepository.findByEmail(email).isPresent());
    }

    @Test
    void joinConflictWhenEmailAlreadyExists() throws Exception {
        JoinRequestDto request = JoinRequestDto.builder()
                .email(userEmail)
                .password(DEFAULT_PASSWORD)
                .nickname("duplicate")
                .build();

        MvcResult result = mockMvc.perform(post("/api/account/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ACCOUNT_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.path").value("/api/account/join"))
                .andReturn();

        ErrorResponseDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ErrorResponseDto.class
        );
        assertNotNull(response.getTraceId());
    }

    @Test
    void joinValidationErrorIncludesTraceId() throws Exception {
        JoinRequestDto request = JoinRequestDto.builder()
                .email("not-an-email")
                .password(DEFAULT_PASSWORD)
                .nickname("user")
                .build();

        mockMvc.perform(post("/api/account/join")
                        .header("X-Trace-Id", "trace-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("Invalid email format"))
                .andExpect(jsonPath("$.traceId").value("trace-123"));
    }

    @Test
    void login() throws Exception {
        LoginRequestDto request = new LoginRequestDto(userEmail, DEFAULT_PASSWORD);

        MvcResult result = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("HttpOnly")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("Path=/api/account")))
                .andReturn();

        TokenResponseDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                TokenResponseDto.class
        );
        assertNotNull(response.getAccessToken());
        assertTrue(response.getExpireIn() > 0);
        assertTrue(jwtRepository.findByAccountId(findAccount(userEmail).getId()).isPresent());
    }

    @Test
    void loginUnauthorizedWhenPasswordIsWrong() throws Exception {
        LoginRequestDto request = new LoginRequestDto(userEmail, "wrong-password");

        mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("The password is incorrect."));
    }

    @Test
    void refresh() throws Exception {
        Cookie refreshCookie = loginAndGetRefreshCookie();

        MvcResult result = mockMvc.perform(post("/api/account/refresh")
                        .cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("refreshToken"))
                .andReturn();

        TokenResponseDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                TokenResponseDto.class
        );
        assertNotNull(response.getAccessToken());

        Cookie rotatedCookie = result.getResponse().getCookie("refreshToken");
        assertNotNull(rotatedCookie);
        assertNotNull(rotatedCookie.getValue());
        assertFalse(rotatedCookie.getValue().isBlank());
        assertEquals(
                rotatedCookie.getValue(),
                jwtRepository.findByAccountId(findAccount(userEmail).getId())
                        .orElseThrow(() -> new IllegalStateException("JWT token not found"))
                        .getRefreshToken()
        );
    }

    @Test
    void refreshUnauthorizedWhenCookieIsMissing() throws Exception {
        mockMvc.perform(post("/api/account/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Refresh token is required."))
                .andExpect(jsonPath("$.path").value("/api/account/refresh"));
    }

    @Test
    void logoutDeletesRefreshToken() throws Exception {
        Cookie refreshCookie = loginAndGetRefreshCookie();
        Long accountId = findAccount(userEmail).getId();
        assertTrue(jwtRepository.findByAccountId(accountId).isPresent());

        mockMvc.perform(post("/api/account/logout")
                        .cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("Max-Age=0")));

        assertFalse(jwtRepository.findByAccountId(accountId).isPresent());
    }

    private Cookie loginAndGetRefreshCookie() throws Exception {
        LoginRequestDto request = new LoginRequestDto(userEmail, DEFAULT_PASSWORD);
        MvcResult result = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        Cookie refreshCookie = result.getResponse().getCookie("refreshToken");
        assertNotNull(refreshCookie);
        return refreshCookie;
    }

    private Account findAccount(String email) {
        return accountRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Account not found: " + email));
    }
}
