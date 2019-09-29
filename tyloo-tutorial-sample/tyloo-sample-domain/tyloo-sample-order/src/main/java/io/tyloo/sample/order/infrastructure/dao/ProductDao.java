package io.tyloo.sample.order.infrastructure.dao;


import io.tyloo.sample.order.domain.entity.Product;

import java.util.List;


public interface ProductDao {

    Product findById(long productId);

    List<Product> findByShopId(long shopId);
}
