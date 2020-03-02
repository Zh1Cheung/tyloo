package io.tyloo.sample.http.capital.api;

import java.math.BigDecimal;

/*
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 10:07 2019/12/5
 *
 */
public interface CapitalAccountService {

    BigDecimal getCapitalAccountByUserId(long userId);
}
