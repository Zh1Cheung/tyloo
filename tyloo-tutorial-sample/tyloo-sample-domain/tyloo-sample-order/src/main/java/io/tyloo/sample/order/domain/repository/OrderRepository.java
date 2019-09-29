package io.tyloo.sample.order.domain.repository;


import io.tyloo.sample.order.domain.entity.Order;
import io.tyloo.sample.order.domain.entity.OrderLine;
import io.tyloo.sample.order.infrastructure.dao.OrderDao;
import io.tyloo.sample.order.infrastructure.dao.OrderLineDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Repository;

/*
 *
 * @Author:Zh1Cheung zh1cheunglq@gmail.com
 * @Date: 18:04 2019/4/19
 *
 */
@Repository
public class OrderRepository {

    @Autowired
    OrderDao orderDao;

    @Autowired
    OrderLineDao orderLineDao;

    public void createOrder(Order order) {
        orderDao.insert(order);

        for (OrderLine orderLine : order.getOrderLines()) {
            orderLineDao.insert(orderLine);
        }
    }

    public void updateOrder(Order order) {
        order.updateVersion();
        int effectCount = orderDao.update(order);

        if (effectCount < 1) {
            throw new OptimisticLockingFailureException("update order failed");
        }
    }

    public Order findByMerchantOrderNo(String merchantOrderNo) {
        return orderDao.findByMerchantOrderNo(merchantOrderNo);
    }
}
