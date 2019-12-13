package io.tyloo.tcctransaction.sample.http.capital.api;

import io.tyloo.api.TransactionContext;
import io.tyloo.tcctransaction.sample.http.capital.api.dto.CapitalTradeOrderDto;

/*
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 10:07 2019/12/5
 *
 */
public interface CapitalTradeOrderService {
    public String record(TransactionContext transactionContext, CapitalTradeOrderDto tradeOrderDto);
}
