package com.store.dto;

import com.store.domain.orders.OrderItem;
import com.store.domain.orders.OrderStatus;
import com.store.domain.orders.Orders;
import com.store.domain.product.ProductColor;
import com.store.domain.product.ProductImage;
import com.store.domain.product.ProductOption;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDto {

    private Long orderId;

    private Long accountId;

    private LocalDateTime orderDate;

    private OrderStatus status;

    private List<OrderItemResponseDto> itemList;

    public static OrderResponseDto from(Orders order) {
        return OrderResponseDto.builder()
                .orderId(order.getId())
                .accountId(order.getAccount().getId())
                .orderDate(order.getOrderDate())
                .status(order.getStatus())
                .itemList(getItemResponseDtos(order))
                .build();
    }

    private static List<OrderItemResponseDto> getItemResponseDtos(Orders order) {
        List<OrderItemResponseDto> itemResponseDtos = new ArrayList<>();
        List<OrderItem> orderItems = order.getOrderItems();
        for (OrderItem item : orderItems) {
            ProductOption option = item.getOption();
            ProductColor color = option.getProductColor();
            itemResponseDtos.add(OrderItemResponseDto.builder()
                    .orderItemId(item.getId())
                    .status(item.getStatus())
                    .quantity(item.getQuantity())
                    .price(item.getPrice())
                    .color(color.getColor())
                    .size(option.getSize())
                    .imageUrl(getMainImageUrl(color))
                    .build());
        }
        return itemResponseDtos;
    }

    private static String getMainImageUrl(ProductColor color) {
        Optional<String> mainImageUrlOpt = color.getImages().stream()
                .filter(ProductImage::isMain)
                .map(ProductImage::getImageUrl)
                .findFirst();
        return mainImageUrlOpt.orElse("");
    }

}
