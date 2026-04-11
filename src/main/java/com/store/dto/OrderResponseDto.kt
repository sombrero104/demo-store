package com.store.dto

import com.store.domain.orders.OrderStatus
import com.store.domain.orders.Orders
import com.store.domain.product.ProductColor
import com.store.domain.product.ProductOption
import java.time.LocalDateTime

data class OrderResponseDto(
    val orderId: Long? = null,
    val accountId: Long? = null,
    val orderDate: LocalDateTime? = null,
    val status: OrderStatus? = null,
    val itemList: List<OrderItemResponseDto> = emptyList(),
) {
    companion object {
        @JvmStatic
        fun from(order: Orders): OrderResponseDto = OrderResponseDto(
            orderId = order.id,
            accountId = order.account.id,
            orderDate = order.orderDate,
            status = order.status,
            itemList = getItemResponseDtos(order),
        )

        private fun getItemResponseDtos(order: Orders): List<OrderItemResponseDto> =
            order.orderItems.map { item ->
                val option: ProductOption = item.option
                val color: ProductColor = option.productColor
                OrderItemResponseDto(
                    orderItemId = item.id,
                    status = item.status,
                    quantity = item.quantity,
                    price = item.price,
                    color = color.color,
                    size = option.size,
                    imageUrl = getMainImageUrl(color),
                )
            }

        private fun getMainImageUrl(color: ProductColor): String =
            color.images.firstOrNull { it.isMain }?.imageUrl ?: ""
    }
}
