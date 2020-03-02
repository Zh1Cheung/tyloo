package io.tyloo.sample.order.domain.factory;

import io.tyloo.sample.order.domain.entity.Order;
import io.tyloo.sample.order.domain.entity.OrderLine;
import javafx.util.Pair;
import io.tyloo.sample.order.domain.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/*
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 9:30 2019/12/5
 *
 */
@Component
public class OrderFactory {

    @Autowired
    ProductRepository productRepository;

    public Order buildOrder(long payerUserId, long payeeUserId, List<Pair<Long, Integer>> productQuantities) {

        Order order = new Order(payerUserId, payeeUserId);

        for (Pair<Long, Integer> pair : productQuantities) {
            long productId = pair.getKey();
            order.addOrderLine(new OrderLine(productId, pair.getValue(), productRepository.findById(productId).getPrice()));
        }

        return order;
    }
}
