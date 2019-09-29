package io.tyloo.sample.order.infrastructure.dao;

import io.tyloo.sample.order.domain.entity.Order;


public interface OrderDao {

    public int insert(Order order);

    public int update(Order order);

    Order findByMerchantOrderNo(String merchantOrderNo);
}
