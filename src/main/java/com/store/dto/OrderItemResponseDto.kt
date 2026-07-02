package com.store.dto

import com.store.domain.orders.OrderItemStatus
import java.math.BigDecimal

data class OrderItemResponseDto(
    val orderItemId: Long? = null,
    val status: OrderItemStatus? = null,
    val quantity: Int = 0,
    val price: BigDecimal = BigDecimal.ZERO,
    val color: String? = null,
    val size: String? = null,
    val imageUrl: String? = null,
)
