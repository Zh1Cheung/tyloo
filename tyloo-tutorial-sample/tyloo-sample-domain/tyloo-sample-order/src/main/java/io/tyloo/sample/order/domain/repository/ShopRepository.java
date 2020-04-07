package io.tyloo.sample.order.domain.repository;

import io.tyloo.sample.order.domain.entity.Shop;
import io.tyloo.sample.order.infrastructure.dao.ShopDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


@Repository
public class ShopRepository {

    @Autowired
    ShopDao shopDao;

    public Shop findById(long id) {

        return shopDao.findById(id);
    }
}
