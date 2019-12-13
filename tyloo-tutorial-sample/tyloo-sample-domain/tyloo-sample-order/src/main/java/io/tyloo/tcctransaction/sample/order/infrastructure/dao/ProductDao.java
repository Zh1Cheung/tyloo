package io.tyloo.tcctransaction.sample.order.infrastructure.dao;


import io.tyloo.tcctransaction.sample.order.domain.entity.Product;

import java.util.List;

/*
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 9:31 2019/12/5
 *
 */
public interface ProductDao {

    Product findById(long productId);

    List<Product> findByShopId(long shopId);
}
