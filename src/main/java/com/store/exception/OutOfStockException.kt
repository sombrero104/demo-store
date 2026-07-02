package com.store.exception

class OutOfStockException(optionId: Long) :
    RuntimeException("재고 부족: optionId=$optionId")
