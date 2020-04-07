package io.tyloo.sample.dubbo.capital.api;

import java.math.BigDecimal;


public interface CapitalAccountService {

    BigDecimal getCapitalAccountByUserId(long userId);
}
