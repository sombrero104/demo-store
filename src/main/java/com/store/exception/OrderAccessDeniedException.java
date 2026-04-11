package com.store.exception;

public class OrderAccessDeniedException extends RuntimeException {

    public OrderAccessDeniedException(Long orderItemId) {
        super("본인의 주문만 취소할 수 있습니다. orderItemId: " + orderItemId);
    }

}
