package io.tyloo.tcctransaction.sample.dubbo.order.service;

import io.tyloo.tcctransaction.CancellingException;
import io.tyloo.tcctransaction.ConfirmingException;
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

        Boolean result = false;

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
            //exception throws with the tcc transaction status is CONFIRMING,
            //when tcc transaction is confirming status,
            // the tcc transaction recovery will try to confirm the whole transaction to ensure eventually consistent.

            result = true;
        } catch (CancellingException cancellingException) {
            //exception throws with the tcc transaction status is CANCELLING,
            //when tcc transaction is under CANCELLING status,
            // the tcc transaction recovery will try to cancel the whole transaction to ensure eventually consistent.
        } catch (Throwable e) {
            //other exceptions throws at TRYING stage.
            //you can retry or cancel the operation.
            e.printStackTrace();
        }

        return order.getMerchantOrderNo();
    }
}
