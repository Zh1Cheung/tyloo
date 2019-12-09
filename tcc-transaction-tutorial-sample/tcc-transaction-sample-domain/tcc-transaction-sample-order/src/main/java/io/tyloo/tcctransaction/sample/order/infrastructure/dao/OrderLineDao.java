package io.tyloo.tcctransaction.sample.order.infrastructure.dao;


import io.tyloo.tcctransaction.sample.order.domain.entity.OrderLine;

/*
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 9:31 2019/12/5
 *
 */
public interface OrderLineDao {
    void insert(OrderLine orderLine);
}
