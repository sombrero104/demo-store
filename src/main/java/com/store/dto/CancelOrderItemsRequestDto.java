package com.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CancelOrderItemsRequestDto {

    @Schema(description = "취소 주문 상품 ID 리스트", example = "[1]")
    @NotEmpty(message = "Order item IDs must not be empty.")
    private List<@NotNull(message = "Order item ID is required.") @Positive(message = "Order item ID must be greater than 0.") Long> orderItemIds;

}
