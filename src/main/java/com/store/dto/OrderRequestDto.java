package com.store.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class OrderRequestDto {

    @NotEmpty(message = "Order items must not be empty.")
    private List<@Valid OrderItemRequestDto> items;

}
