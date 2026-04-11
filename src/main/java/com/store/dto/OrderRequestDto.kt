package com.store.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty

data class OrderRequestDto(
    @field:NotEmpty(message = "Order items must not be empty.")
    val items: List<@Valid OrderItemRequestDto>,
)
