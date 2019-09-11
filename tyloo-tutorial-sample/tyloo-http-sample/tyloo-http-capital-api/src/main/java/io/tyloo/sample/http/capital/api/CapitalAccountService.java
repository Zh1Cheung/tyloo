package io.tyloo.sample.http.capital.api;

import java.math.BigDecimal;

public interface CapitalAccountService {

    BigDecimal getCapitalAccountByUserId(long userId);
}
