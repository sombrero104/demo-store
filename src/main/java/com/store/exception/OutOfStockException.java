package com.store.exception;

public class OutOfStockException extends RuntimeException {

    public OutOfStockException(Long optionId) {
        super("재고 부족: optionId=" + optionId);
    }

}
