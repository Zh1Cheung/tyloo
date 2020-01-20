package io.tyloo.tcctransaction.sample.dubbo.redpacket.api;

import io.tyloo.api.Tyloo;
import io.tyloo.tcctransaction.sample.dubbo.redpacket.api.dto.RedPacketTradeOrderDto;

/*
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 9:27 2019/12/5
 *
 */
public interface RedPacketTradeOrderService {

    @Tyloo
    public String record(RedPacketTradeOrderDto tradeOrderDto);
}
