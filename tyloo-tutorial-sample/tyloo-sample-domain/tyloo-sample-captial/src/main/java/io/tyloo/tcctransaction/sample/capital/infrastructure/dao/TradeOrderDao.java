package io.tyloo.tcctransaction.sample.capital.infrastructure.dao;


import io.tyloo.tcctransaction.sample.capital.domain.entity.TradeOrder;

/*
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 9:28 2019/12/5
 *
 */
public interface TradeOrderDao {

    int insert(TradeOrder tradeOrder);

    int update(TradeOrder tradeOrder);

    TradeOrder findByMerchantOrderNo(String merchantOrderNo);
}
