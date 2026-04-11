package com.store.controller;

import com.store.auth.CurrentUserName;
import com.store.domain.account.AccountRole;
import com.store.dto.CancelOrderItemsRequestDto;
import com.store.dto.MessageResponseDto;
import com.store.dto.OrderRequestDto;
import com.store.dto.OrderResponseDto;
import com.store.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@Validated
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    @PreAuthorize(AccountRole.ORDER_READ)
    public ResponseEntity<List<OrderResponseDto>> getOrderList(
            @RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = "Page must be 0 or greater.") int page,
            @RequestParam(value = "size", defaultValue = "5") @Min(value = 1, message = "Size must be at least 1.") @Max(value = 100, message = "Size must be 100 or less.") int size,
            @CurrentUserName String currentUserEmail
    ) {
        List<OrderResponseDto> orderResponseDtoList = orderService.getOrderList(currentUserEmail, page, size);
        return ResponseEntity.ok().body(orderResponseDtoList);
    }

    @PostMapping
    @PreAuthorize(AccountRole.ORDER_WRITE)
    public ResponseEntity<MessageResponseDto> placeOrder(
            @Valid @RequestBody OrderRequestDto dto,
            @CurrentUserName String currentUserEmail
    ) {
        MessageResponseDto messageResponseDto = orderService.placeOrder(currentUserEmail, dto);
        return ResponseEntity.ok().body(messageResponseDto);
    }

    @PostMapping("/cancel/items")
    @PreAuthorize(AccountRole.ORDER_WRITE)
    public ResponseEntity<MessageResponseDto> cancelOrderItems(
            @Valid @RequestBody CancelOrderItemsRequestDto requestDto,
            @CurrentUserName String currentUserEmail
    ) {
        MessageResponseDto response = orderService.cancelOrderItems(currentUserEmail, requestDto.getOrderItemIds());
        return ResponseEntity.ok().body(response);
    }

}
