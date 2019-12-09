package io.tyloo.tcctransaction.sample.dubbo.capital.api;

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

    BigDecimal getCapitalAccountByUserId(long userId);
}
