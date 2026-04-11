package com.store.domain.orders;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.store.domain.account.QAccount;
import com.store.domain.product.QProductColor;
import com.store.domain.product.QProductOption;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Long> findOrderIdsByAccountId(Long accountId, Pageable pageable) {
        QOrders orders = QOrders.orders;

        return queryFactory
                .select(orders.id)
                .from(orders)
                .where(orders.account.id.eq(accountId))
                .orderBy(orders.orderDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public List<Orders> findDetailOrdersByIds(List<Long> orderIds) {
        if (orderIds.isEmpty()) {
            return List.of();
        }

        QOrders orders = QOrders.orders;
        QAccount account = QAccount.account;
        QOrderItem orderItem = QOrderItem.orderItem;
        QProductOption productOption = QProductOption.productOption;
        QProductColor productColor = QProductColor.productColor;

        return queryFactory
                .selectDistinct(orders)
                .from(orders)
                .join(orders.account, account).fetchJoin()
                .leftJoin(orders.orderItems, orderItem).fetchJoin()
                .leftJoin(orderItem.option, productOption).fetchJoin()
                .leftJoin(productOption.productColor, productColor).fetchJoin()
                .where(orders.id.in(orderIds))
                .orderBy(orders.orderDate.desc())
                .fetch();
    }
}
