package io.tyloo.sample.dubbo.capital.api;

import java.math.BigDecimal;

/*
 *
 * 资金账户服务
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 8:57 2019/12/5
 *
 */
public interface CapitalAccountService {

    /**
     * 获得资金账户金额
     *
     * @param userId
     * @return
     */
    BigDecimal getCapitalAccountByUserId(long userId);
}
