package io.tyloo.sample.order.infrastructure.dao;


import io.tyloo.sample.order.domain.entity.OrderLine;


public interface OrderLineDao {
    void insert(OrderLine orderLine);
}
