package com.store.controller.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.domain.account.Account;
import com.store.domain.account.AccountRepository;
import com.store.domain.account.RoleGroup;
import com.store.domain.account.RoleGroupRepository;
import com.store.domain.product.ProductOption;
import com.store.domain.product.ProductOptionRepository;
import com.store.dto.OrderItemRequestDto;
import com.store.dto.OrderRequestDto;
import com.store.dto.TokenResponseDto;
import com.store.dto.ai.AiChatIntent;
import com.store.dto.ai.AiChatRequestDto;
import com.store.service.ai.AiChatDecision;
import com.store.service.ai.AiIntentClientRouter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/sql/test-reset.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class AiChatControllerTest {

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
    private ProductOptionRepository productOptionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private AiIntentClientRouter aiIntentClientRouter;

    @MockitoBean
    private RedissonClient redissonClient;

    @MockitoBean
    private RLock orderLock;

    private String userEmail;
    private Long orderProductOptionId;

    @BeforeEach
    void setUpData() throws InterruptedException {
        given(redissonClient.getLock(anyString())).willReturn(orderLock);
        given(orderLock.tryLock(eq(3L), eq(java.util.concurrent.TimeUnit.SECONDS))).willReturn(true);
        given(orderLock.isHeldByCurrentThread()).willReturn(true);

        long suffix = System.nanoTime();
        userEmail = "user" + suffix + "@store.com";
        orderProductOptionId = productOptionRepository.findAll().stream()
                .findFirst()
                .map(ProductOption::getId)
                .orElseThrow(() -> new IllegalStateException("No product option for test."));

        RoleGroup userGroup = roleGroupRepository.findByCode("USER")
                .orElseThrow(() -> new IllegalStateException("USER role group not found"));

        Account user = Account.ofSignUp(userEmail, passwordEncoder.encode(DEFAULT_PASSWORD), "user");
        user.addRoleGroup(userGroup);
        accountRepository.save(user);
    }

    @Test
    void chatReturnsOrderSummary() throws Exception {
        placeOrder();
        given(aiIntentClientRouter.decide(anyString()))
                .willReturn(new AiChatDecision(AiChatIntent.ORDER_LIST, "주문 내역을 확인해드릴게요."));

        mockMvc.perform(post("/api/ai/chat")
                        .header("Authorization", "Bearer " + getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AiChatRequestDto("내 주문 내역 보여줘"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intent").value(AiChatIntent.ORDER_LIST.name()))
                .andExpect(jsonPath("$.answer").value(containsString("최근 주문 1건")));
    }

    @Test
    void chatUnauthorizedWhenNoToken() throws Exception {
        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AiChatRequestDto("내 주문 내역 보여줘"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void chatValidationErrorWhenMessageIsBlank() throws Exception {
        given(aiIntentClientRouter.decide(anyString()))
                .willReturn(new AiChatDecision(AiChatIntent.UNKNOWN, "지원하지 않는 요청입니다."));

        mockMvc.perform(post("/api/ai/chat")
                        .header("Authorization", "Bearer " + getAccessToken())
                        .header("X-Trace-Id", "trace-ai-chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AiChatRequestDto(" "))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("Message cannot be empty."))
                .andExpect(jsonPath("$.traceId").value("trace-ai-chat"));
    }

    @Test
    void chatReturnsServiceUnavailableWhenAiIsDisabled() throws Exception {
        given(aiIntentClientRouter.decide(anyString()))
                .willThrow(new com.store.exception.AiIntegrationException("AI service is disabled."));

        mockMvc.perform(post("/api/ai/chat")
                        .header("Authorization", "Bearer " + getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AiChatRequestDto("내 주문 내역 보여줘"))))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("AI_UNAVAILABLE"));
    }

    private void placeOrder() throws Exception {
        List<OrderItemRequestDto> items = List.of(new OrderItemRequestDto(orderProductOptionId, 1));

        mockMvc.perform(post("/api/order")
                        .header("Authorization", "Bearer " + getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new OrderRequestDto(items))))
                .andExpect(status().isOk());
    }

    private String getAccessToken() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new com.store.dto.LoginRequestDto(userEmail, DEFAULT_PASSWORD))))
                .andExpect(status().isOk())
                .andReturn();

        TokenResponseDto tokenResponseDto = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                TokenResponseDto.class
        );
        return tokenResponseDto.getAccessToken();
    }
}
