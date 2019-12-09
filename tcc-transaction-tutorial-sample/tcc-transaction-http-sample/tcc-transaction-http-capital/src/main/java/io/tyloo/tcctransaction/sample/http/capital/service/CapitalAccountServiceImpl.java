package io.tyloo.tcctransaction.sample.http.capital.service;

import io.tyloo.tcctransaction.sample.capital.domain.repository.CapitalAccountRepository;
import io.tyloo.tcctransaction.sample.http.capital.api.CapitalAccountService;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

/*
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 10:07 2019/12/5
 *
 */
public class CapitalAccountServiceImpl implements CapitalAccountService{


    @Autowired
    CapitalAccountRepository capitalAccountRepository;

    @Override
    public BigDecimal getCapitalAccountByUserId(long userId) {
        return capitalAccountRepository.findByUserId(userId).getBalanceAmount();
    }
}
