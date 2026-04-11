package com.store.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.domain.account.Account;
import com.store.domain.account.AccountRepository;
import com.store.domain.account.RoleGroup;
import com.store.domain.account.RoleGroupRepository;
import com.store.domain.product.ProductOption;
import com.store.domain.product.ProductOptionRepository;
import com.store.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/sql/test-reset.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class OrderControllerTest {
    private static final String DEFAULT_PASSWORD = "1234";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RoleGroupRepository roleGroupRepository;

    @Autowired
    private ProductOptionRepository productOptionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String userEmail;
    private Long orderProductOptionId;

    @BeforeEach
    void setUpData() {
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
    void getOrderList() throws Exception {
        order();
        mockMvc.perform(get("/api/order")
                        .header("Authorization", "Bearer " + getAccessToken()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void placeOrder() throws Exception {
        order();
    }

    @Test
    void cancelOrderItems() throws Exception {
        order();
        Long orderItemId = getLatestOrderItemId(getAccessToken());
        mockMvc.perform(post("/api/order/cancel/items")
                        .header("Authorization", "Bearer " + getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CancelOrderItemsRequestDto(List.of(orderItemId)))))
                .andDo(print())
                .andExpect(status().isOk());
    }

    private void order() throws Exception {
        List<OrderItemRequestDto> items = List.of(new OrderItemRequestDto(orderProductOptionId, 1));

        mockMvc.perform(post("/api/order")
                        .header("Authorization", "Bearer " + getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new OrderRequestDto(items))))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void unauthorizedWhenNoToken() throws Exception {
        mockMvc.perform(get("/api/order"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    private String getAccessToken() throws Exception {
        LoginRequestDto loginRequestDto = new LoginRequestDto(userEmail, DEFAULT_PASSWORD);
        MvcResult mvcResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        TokenResponseDto tokenResponseDto = objectMapper.readValue(responseContent, TokenResponseDto.class);
        return tokenResponseDto.getAccessToken();
    }

    private Long getLatestOrderItemId(String accessToken) throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/api/order")
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        List<OrderResponseDto> orders = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {}
        );

        return orders.stream()
                .findFirst()
                .flatMap(order -> order.getItemList().stream().findFirst())
                .map(OrderItemResponseDto::getOrderItemId)
                .orElseThrow(() -> new IllegalStateException("No order item found."));
    }

}
