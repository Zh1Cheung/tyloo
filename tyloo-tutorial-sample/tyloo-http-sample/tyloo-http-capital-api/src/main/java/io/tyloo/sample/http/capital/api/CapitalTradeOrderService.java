package io.tyloo.sample.http.capital.api;

import io.tyloo.sample.http.capital.api.dto.CapitalTradeOrderDto;
import io.tyloo.api.TransactionContext;


public interface CapitalTradeOrderService {
    public String record(TransactionContext transactionContext, CapitalTradeOrderDto tradeOrderDto);
}
