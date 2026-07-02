package com.store.exception

class OrderAccessDeniedException(orderItemId: Long) :
    RuntimeException("본인의 주문만 취소할 수 있습니다. orderItemId: $orderItemId")
