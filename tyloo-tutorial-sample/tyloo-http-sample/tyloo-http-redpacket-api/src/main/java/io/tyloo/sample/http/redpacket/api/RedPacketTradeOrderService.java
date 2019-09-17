package io.tyloo.sample.http.redpacket.api;

import io.tyloo.sample.http.redpacket.api.dto.RedPacketTradeOrderDto;
import io.tyloo.api.TransactionContext;


public interface RedPacketTradeOrderService {

    public String record(TransactionContext transactionContext, RedPacketTradeOrderDto tradeOrderDto);
}
