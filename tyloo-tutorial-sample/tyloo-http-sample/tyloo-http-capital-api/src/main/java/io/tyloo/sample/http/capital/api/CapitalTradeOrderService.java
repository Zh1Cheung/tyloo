package io.tyloo.sample.http.capital.api;

import io.tyloo.api.Context.TylooTransactionContext;
import io.tyloo.sample.http.capital.api.dto.CapitalTradeOrderDto;

/*
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 10:07 2019/12/5
 *
 */
public interface CapitalTradeOrderService {
    public String record(TylooTransactionContext transactionContext, CapitalTradeOrderDto tradeOrderDto);
        }
