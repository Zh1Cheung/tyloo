package io.tyloo.tcctransaction.sample.dubbo.order.service;

import io.tyloo.tcctransaction.sample.dubbo.capital.api.CapitalTradeOrderService;
import io.tyloo.tcctransaction.sample.dubbo.capital.api.dto.CapitalTradeOrderDto;
import io.tyloo.tcctransaction.sample.dubbo.redpacket.api.RedPacketTradeOrderService;
import io.tyloo.tcctransaction.sample.dubbo.redpacket.api.dto.RedPacketTradeOrderDto;
import io.tyloo.tcctransaction.sample.order.domain.entity.Order;
import io.tyloo.tcctransaction.sample.order.domain.repository.OrderRepository;
import org.apache.commons.lang3.time.DateFormatUtils;
import io.tyloo.api.Compensable;
import io.tyloo.api.UniqueIdentity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.util.Calendar;

/*
 *
 * 付款实现类
 *
 * 如果事务日志没有成功提交，那么整个TCC事务将会需要恢复，
 * 如果是在 CONFIRMING 阶段出异常，则恢复Job将继续启动事务的 Confirm 操作过程，
 * 如果是在 TRYING 阶段出异常，则恢复Job将启动事务的 Cancel 操作过程。
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 9:07 2019/12/5
 *
 */
@Service
public class PaymentServiceImpl {

    @Autowired
    CapitalTradeOrderService capitalTradeOrderService;

    @Autowired
    RedPacketTradeOrderService redPacketTradeOrderService;

    @Autowired
    OrderRepository orderRepository;


    /**
     * 付款.
     *
     * @param order              订单信息.
     * @param redPacketPayAmount 红包支付金额
     * @param capitalPayAmount   资金帐户支付金额.
     */
    @Compensable(confirmMethod = "confirmMakePayment", cancelMethod = "cancelMakePayment", asyncConfirm = false, delayCancelExceptions = {SocketTimeoutException.class, com.alibaba.dubbo.remoting.TimeoutException.class})
    public void makePayment(@UniqueIdentity String orderNo, Order order, BigDecimal redPacketPayAmount, BigDecimal capitalPayAmount) {
        System.out.println("order try make payment called.time seq:" + DateFormatUtils.format(Calendar.getInstance(), "yyyy-MM-dd HH:mm:ss"));

        //检查订单状态是否为DRAFT，如果不是，则表示发生了同一订单的另一个调用makePayment，忽略此调用makePayment。
        if (order.getStatus().equals("DRAFT")) {
            order.pay(redPacketPayAmount, capitalPayAmount);
            try {
                orderRepository.updateOrder(order);
            } catch (OptimisticLockingFailureException e) {
                //忽略并发更新顺序异常，确保幂等性。
            }
        }
        /*
          资金帐户交易订单记录
          没有 贴@Compensable注解，所以会且只会被第二个切面切到

          此时的capitalTradeOrderService是本地的一个代理类，所以这个 record 方法实际上是本地代理对象中的一个方法，
          在这个方法里中才通过dubbo(RPC)调用远程的record业务方法返回结果,真正的业务方法的实现是在tcyloo-dubbo-capital模块内

         */
        /*
          capitalTradeOrderService和redPacketTradeOrderService是dubbo动态代理出来本地代理类
         */
        String result = capitalTradeOrderService.record(buildCapitalTradeOrderDto(order));
        // 红包帐户交易订单记录
        String result2 = redPacketTradeOrderService.record(buildRedPacketTradeOrderDto(order));
    }

    /**
     * 确认付款.
     *
     * @param order
     * @param redPacketPayAmount
     * @param capitalPayAmount
     */
    public void confirmMakePayment(String orderNo, Order order, BigDecimal redPacketPayAmount, BigDecimal capitalPayAmount) {

        try {
            Thread.sleep(1000l);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("order confirm make payment called. time seq:" + DateFormatUtils.format(Calendar.getInstance(), "yyyy-MM-dd HH:mm:ss"));

        Order foundOrder = orderRepository.findByMerchantOrderNo(order.getMerchantOrderNo());

        //检查交易订单状态是否为 PAYING ，如果为否，则表示发生了另一个呼叫确认支付，直接返回，确保等幂性。
        if (foundOrder != null && foundOrder.getStatus().equals("PAYING")) {
            order.confirm();
            orderRepository.updateOrder(order);
        }
    }

    /**
     * 取消付款.
     *
     * @param order
     * @param redPacketPayAmount
     * @param capitalPayAmount
     */
    public void cancelMakePayment(String orderNo, Order order, BigDecimal redPacketPayAmount, BigDecimal capitalPayAmount) {

        try {
            Thread.sleep(1000l);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("order cancel make payment called.time seq:" + DateFormatUtils.format(Calendar.getInstance(), "yyyy-MM-dd HH:mm:ss"));

        Order foundOrder = orderRepository.findByMerchantOrderNo(order.getMerchantOrderNo());

        //检查交易订单状态是否为 PAYING ，如果为否，则表示发生了另一个调用cancelMakePayment，直接返回，确保等幂性。
        if (foundOrder != null && foundOrder.getStatus().equals("PAYING")) {
            order.cancelPayment();
            orderRepository.updateOrder(order);
        }
    }

    /**
     * 构建资金帐户支付订单Dto
     *
     * @param order
     * @return
     */
    private CapitalTradeOrderDto buildCapitalTradeOrderDto(Order order) {

        CapitalTradeOrderDto tradeOrderDto = new CapitalTradeOrderDto();
        tradeOrderDto.setAmount(order.getCapitalPayAmount());
        tradeOrderDto.setMerchantOrderNo(order.getMerchantOrderNo());
        tradeOrderDto.setSelfUserId(order.getPayerUserId());
        tradeOrderDto.setOppositeUserId(order.getPayeeUserId());
        tradeOrderDto.setOrderTitle(String.format("order no:%s", order.getMerchantOrderNo()));

        return tradeOrderDto;
    }

    /**
     * 构建红包帐户支付订单Dto
     *
     * @param order
     * @return
     */
    private RedPacketTradeOrderDto buildRedPacketTradeOrderDto(Order order) {
        RedPacketTradeOrderDto tradeOrderDto = new RedPacketTradeOrderDto();
        tradeOrderDto.setAmount(order.getRedPacketPayAmount());
        tradeOrderDto.setMerchantOrderNo(order.getMerchantOrderNo());
        tradeOrderDto.setSelfUserId(order.getPayerUserId());
        tradeOrderDto.setOppositeUserId(order.getPayeeUserId());
        tradeOrderDto.setOrderTitle(String.format("order no:%s", order.getMerchantOrderNo()));

        return tradeOrderDto;
    }
}
