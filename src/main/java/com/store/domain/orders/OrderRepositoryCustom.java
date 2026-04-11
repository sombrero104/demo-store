package com.store.domain.orders;

import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderRepositoryCustom {

    List<Long> findOrderIdsByAccountId(Long accountId, Pageable pageable);

    List<Orders> findDetailOrdersByIds(List<Long> orderIds);

}
