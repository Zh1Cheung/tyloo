package io.tyloo.tcctransaction.sample.dubbo.capital.api;

import io.tyloo.api.Compensable;
import io.tyloo.tcctransaction.sample.dubbo.capital.api.dto.CapitalTradeOrderDto;

/*
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 8:57 2019/12/5
 *
 */
public interface CapitalTradeOrderService {
    /**
     * 创建资金帐户变更记录.
     *
     * @param tradeOrderDto
     */
    @Compensable
    public String record(CapitalTradeOrderDto tradeOrderDto);

}
