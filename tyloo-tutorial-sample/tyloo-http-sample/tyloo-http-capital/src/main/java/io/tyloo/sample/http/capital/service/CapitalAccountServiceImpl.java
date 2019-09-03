package io.tyloo.sample.http.capital.service;

import io.tyloo.sample.capital.domain.repository.CapitalAccountRepository;
import io.tyloo.sample.http.capital.api.CapitalAccountService;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;


public class CapitalAccountServiceImpl implements CapitalAccountService {


    @Autowired
    CapitalAccountRepository capitalAccountRepository;

    @Override
    public BigDecimal getCapitalAccountByUserId(long userId) {
        return capitalAccountRepository.findByUserId(userId).getBalanceAmount();
    }
}
