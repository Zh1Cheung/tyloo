package io.tyloo.tcctransaction.sample.http.order.service;

import io.tyloo.api.Compensable;
import io.tyloo.api.Propagation;
import io.tyloo.api.TransactionContext;
import io.tyloo.tcctransaction.context.MethodTransactionContextEditor;
import io.tyloo.tcctransaction.sample.http.capital.api.CapitalTradeOrderService;
import io.tyloo.tcctransaction.sample.http.capital.api.dto.CapitalTradeOrderDto;
import io.tyloo.tcctransaction.sample.http.redpacket.api.RedPacketTradeOrderService;
import io.tyloo.tcctransaction.sample.http.redpacket.api.dto.RedPacketTradeOrderDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/*
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 10:07 2019/12/5
 *
 */
@Component
public class TradeOrderServiceProxy {

    @Autowired
    CapitalTradeOrderService capitalTradeOrderService;

    @Autowired
    RedPacketTradeOrderService redPacketTradeOrderService;

    /*the propagation need set Propagation.SUPPORTS,otherwise the recover doesn't work,
      The default value is Propagation.REQUIRED, which means will begin new transaction when recover.
    */
    @Compensable(propagation = Propagation.SUPPORTS, confirmMethod = "record", cancelMethod = "record", transactionContextEditor = MethodTransactionContextEditor.class)
    public String record(TransactionContext transactionContext, CapitalTradeOrderDto tradeOrderDto) {
        return capitalTradeOrderService.record(transactionContext, tradeOrderDto);
    }

    @Compensable(propagation = Propagation.SUPPORTS, confirmMethod = "record", cancelMethod = "record", transactionContextEditor = MethodTransactionContextEditor.class)
    public String record(TransactionContext transactionContext, RedPacketTradeOrderDto tradeOrderDto) {
        return redPacketTradeOrderService.record(transactionContext, tradeOrderDto);
    }
}
