package io.tyloo.sample.order.infrastructure.dao;


import io.tyloo.sample.order.domain.entity.Shop;

/*
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 9:31 2019/12/5
 *
 */
public interface ShopDao {
    Shop findById(long id);
}
