package com.store.exception;

public class OrderLockException extends RuntimeException {

    public OrderLockException(Long productOptionId) {
        super("주문 처리 중 락 획득 실패: optionId=" + productOptionId);
    }

    public OrderLockException(Long productOptionId, Throwable cause) {
        super("주문 처리 중 락 획득 실패: optionId=" + productOptionId, cause);
    }
}
