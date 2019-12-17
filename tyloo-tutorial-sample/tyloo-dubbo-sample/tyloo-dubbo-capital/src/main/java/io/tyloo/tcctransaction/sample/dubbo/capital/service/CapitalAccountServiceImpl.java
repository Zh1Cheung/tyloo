package io.tyloo.tcctransaction.sample.dubbo.capital.service;

import io.tyloo.tcctransaction.sample.capital.domain.repository.CapitalAccountRepository;
import io.tyloo.tcctransaction.sample.dubbo.capital.api.CapitalAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/*
 *
 * 资金账户实现类
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 8:57 2019/12/5
 *
 */

@Service("capitalAccountService")
public class CapitalAccountServiceImpl implements CapitalAccountService {


    @Autowired
    CapitalAccountRepository capitalAccountRepository;

    /**
     * 获得资金账户金额
     *
     * @param userId
     * @return
     */
    @Override
    public BigDecimal getCapitalAccountByUserId(long userId) {
        return capitalAccountRepository.findByUserId(userId).getBalanceAmount();
    }
}
