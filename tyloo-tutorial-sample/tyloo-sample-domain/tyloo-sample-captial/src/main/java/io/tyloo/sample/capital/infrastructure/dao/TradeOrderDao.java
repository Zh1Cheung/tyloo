package io.tyloo.sample.capital.infrastructure.dao;


import io.tyloo.sample.capital.domain.entity.TradeOrder;


public interface TradeOrderDao {

    int insert(TradeOrder tradeOrder);

    int update(TradeOrder tradeOrder);

    TradeOrder findByMerchantOrderNo(String merchantOrderNo);
}
