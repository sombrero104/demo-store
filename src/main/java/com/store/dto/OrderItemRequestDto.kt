package com.store.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Positive

data class OrderItemRequestDto(

    @Schema(description = "상품 옵션 ID", example = "1")
    @field:Positive(message = "Product option ID must be greater than 0.")
    val productOptionId: Long,

    @Schema(description = "상품 수량", example = "1")
    @field:Min(value = 1, message = "Quantity must be at least 1.")
    val quantity: Int

)