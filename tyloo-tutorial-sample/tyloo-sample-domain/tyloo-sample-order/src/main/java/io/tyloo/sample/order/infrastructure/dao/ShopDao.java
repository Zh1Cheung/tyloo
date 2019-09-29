package io.tyloo.sample.order.infrastructure.dao;


import io.tyloo.sample.order.domain.entity.Shop;


public interface ShopDao {
    Shop findById(long id);
}
