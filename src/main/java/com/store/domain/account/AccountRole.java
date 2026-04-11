package com.store.domain.account;

public final class AccountRole {

    private AccountRole() {
    }

    public static final String ORDER_READ = "hasAuthority('ORDER_READ')"; // 주문 조회

    public static final String ORDER_WRITE = "hasAuthority('ORDER_WRITE')"; // 주문 생성/취소

    public static final String PRODUCT_READ = "hasAuthority('PRODUCT_READ')"; // 상품 조회

    public static final String PRODUCT_WRITE = "hasAuthority('PRODUCT_WRITE')"; // 상품 등록/수정
}
