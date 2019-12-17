package io.tyloo.tcctransaction.sample.dubbo.order.service;

import io.tyloo.tcctransaction.exception.CancellingException;
import io.tyloo.tcctransaction.exception.ConfirmingException;
import io.tyloo.tcctransaction.sample.order.domain.entity.Order;
import io.tyloo.tcctransaction.sample.order.domain.entity.Shop;
import io.tyloo.tcctransaction.sample.order.domain.repository.ShopRepository;
import io.tyloo.tcctransaction.sample.order.domain.service.OrderServiceImpl;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/*
 *
 * 下订单实现类
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 9:09 2019/12/5
 *
 */
@Service
public class PlaceOrderServiceImpl {

    @Autowired
    ShopRepository shopRepository;

    @Autowired
    OrderServiceImpl orderService;

    @Autowired
    PaymentServiceImpl paymentService;

    /**
     * 下订单。
     *
     * @param payerUserId        付款者ID.
     * @param shopId             店铺ID.
     * @param productQuantities  产品数量
     * @param redPacketPayAmount 红包支付金额。
     */
    public String placeOrder(long payerUserId, long shopId, List<Pair<Long, Integer>> productQuantities, final BigDecimal redPacketPayAmount) {
        // 查找店铺信息，主要目的用于获取收款者ID
        Shop shop = shopRepository.findById(shopId);
        // 创建订单
        final Order order = orderService.createOrder(payerUserId, shop.getOwnerUserId(), productQuantities);

        boolean result = false;

        try {

//            ExecutorService executorService = Executors.newFixedThreadPool(2);

//            Future future1 = executorService.submit(new Runnable() {
//                @Override
//                public void run() {
            // 付款
            paymentService.makePayment(order.getMerchantOrderNo(), order, redPacketPayAmount, order.getTotalAmount().subtract(redPacketPayAmount));
//                }
//            });

//            Future future2 = executorService.submit(new Runnable() {
//                @Override
//                public void run() {
//                    paymentService.makePayment(order.getMerchantOrderNo(), order, redPacketPayAmount, order.getTotalAmount().subtract(redPacketPayAmount));
//                }
//            });
//
//            future1.get();
//            future2.get();

        } catch (ConfirmingException confirmingException) {
            //异常抛出，tcc事务状态为 CONFIRMING ，
            //当tcc事务正在 CONFIRMING 状态时，
            //tcc事务恢复将尝试确认整个事务，以确保最终一致。
            result = true;
        } catch (CancellingException cancellingException) {
            //异常抛出，tcc事务状态为 CANCELLING ，
            //当tcc事务处于 CANCELLING 状态时，
            //tcc事务恢复将尝试取消整个事务，以确保最终一致。
        } catch (Throwable e) {
            //其他的例外在 TRYING 阶段抛出。
            //您可以重试或取消该操作。
            e.printStackTrace();
        }

        return order.getMerchantOrderNo();
    }
}
