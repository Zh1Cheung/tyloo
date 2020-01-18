package io.tyloo.sample.dubbo.capital.api;

import io.tyloo.sample.dubbo.capital.api.dto.CapitalTradeOrderDto;
import io.tyloo.api.Tyloo;


public interface CapitalTradeOrderService {

    @Tyloo
    public String record(CapitalTradeOrderDto tradeOrderDto);

}
