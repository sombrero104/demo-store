package com.store.exception

class OrderLockException : RuntimeException {
    constructor(productOptionId: Long) :
        super("주문 처리 중 락 획득 실패: optionId=$productOptionId")

    constructor(productOptionId: Long, cause: Throwable) :
        super("주문 처리 중 락 획득 실패: optionId=$productOptionId", cause)
}
