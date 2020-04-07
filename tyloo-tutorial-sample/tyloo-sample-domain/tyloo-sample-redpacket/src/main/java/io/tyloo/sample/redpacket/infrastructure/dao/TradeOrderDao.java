package io.tyloo.sample.redpacket.infrastructure.dao;


import io.tyloo.sample.redpacket.domain.entity.TradeOrder;


public interface TradeOrderDao {

    void insert(TradeOrder tradeOrder);

    int update(TradeOrder tradeOrder);

    TradeOrder findByMerchantOrderNo(String merchantOrderNo);
}
