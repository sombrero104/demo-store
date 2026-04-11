package com.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderItemRequestDto {

    @Schema(description = "상품 옵션 ID", example = "1")
    @NotNull(message = "Product option ID is required.")
    @Positive(message = "Product option ID must be greater than 0.")
    private Long productOptionId;

    @Schema(description = "상품 수량", example = "1")
    @Min(value = 1, message = "Quantity must be at least 1.")
    private int quantity;

}
