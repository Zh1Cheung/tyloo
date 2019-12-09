package io.tyloo.tcctransaction.sample.order.infrastructure.dao;

import io.tyloo.tcctransaction.sample.order.domain.entity.Order;

/*
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 9:30 2019/12/5
 *
 */
public interface OrderDao {

    public int insert(Order order);

    public int update(Order order);

    Order findByMerchantOrderNo(String merchantOrderNo);
}
