package com.store.dto;

import com.store.domain.orders.OrderItemStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponseDto {

    private Long orderItemId;

    private OrderItemStatus status;

    private int quantity;

    private int price;

    private String color;

    private String size;

    private String imageUrl;

}
