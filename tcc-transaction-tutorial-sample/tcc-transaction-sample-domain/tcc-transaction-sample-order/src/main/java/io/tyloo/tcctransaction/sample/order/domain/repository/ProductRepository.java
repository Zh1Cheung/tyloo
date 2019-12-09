package io.tyloo.tcctransaction.sample.order.domain.repository;

import io.tyloo.tcctransaction.sample.order.domain.entity.Product;
import io.tyloo.tcctransaction.sample.order.infrastructure.dao.ProductDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 9:30 2019/12/5
 *
 */
@Repository
public class ProductRepository {

    @Autowired
    ProductDao productDao;

    public Product findById(long productId){
        return productDao.findById(productId);
    }

    public List<Product> findByShopId(long shopId){
        return productDao.findByShopId(shopId);
    }
}
