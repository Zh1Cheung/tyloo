package io.tyloo.sample.http.order.service;

import io.tyloo.sample.http.capital.api.CapitalTradeOrderService;
import io.tyloo.sample.http.capital.api.dto.CapitalTradeOrderDto;
import io.tyloo.sample.http.redpacket.api.RedPacketTradeOrderService;
import io.tyloo.sample.http.redpacket.api.dto.RedPacketTradeOrderDto;
import io.tyloo.api.Tyloo;
import io.tyloo.api.Propagation;
import io.tyloo.api.TransactionContext;
import io.tyloo.context.MethodTransactionContextEditor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class TradeOrderServiceProxy {

    @Autowired
    CapitalTradeOrderService capitalTradeOrderService;

    @Autowired
    RedPacketTradeOrderService redPacketTradeOrderService;

    /*the propagation need set Propagation.SUPPORTS,otherwise the recover doesn't work,
      The default value is Propagation.REQUIRED, which means will begin new transaction when recover.
    */
    @Tyloo(propagation = Propagation.SUPPORTS, confirmMethod = "record", cancelMethod = "record", transactionContextEditor = MethodTransactionContextEditor.class)
    public String record(TransactionContext transactionContext, CapitalTradeOrderDto tradeOrderDto) {
        return capitalTradeOrderService.record(transactionContext, tradeOrderDto);
    }

    @Tyloo(propagation = Propagation.SUPPORTS, confirmMethod = "record", cancelMethod = "record", transactionContextEditor = MethodTransactionContextEditor.class)
    public String record(TransactionContext transactionContext, RedPacketTradeOrderDto tradeOrderDto) {
        return redPacketTradeOrderService.record(transactionContext, tradeOrderDto);
    }
}
