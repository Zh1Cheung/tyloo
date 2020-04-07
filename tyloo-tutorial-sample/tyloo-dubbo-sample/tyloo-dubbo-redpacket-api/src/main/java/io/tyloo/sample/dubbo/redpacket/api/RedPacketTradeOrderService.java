package io.tyloo.sample.dubbo.redpacket.api;

import io.tyloo.sample.dubbo.redpacket.api.dto.RedPacketTradeOrderDto;
import io.tyloo.api.Tyloo;


public interface RedPacketTradeOrderService {

    @Tyloo
    public String record(RedPacketTradeOrderDto tradeOrderDto);
}
