package io.tyloo.sample.http.order.service;

import io.tyloo.sample.http.capital.api.CapitalAccountService;
import io.tyloo.sample.http.redpacket.api.RedPacketAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


@Service("accountService")
public class AccountServiceImpl {

    @Autowired
    RedPacketAccountService redPacketAccountService;

    @Autowired
    CapitalAccountService capitalAccountService;


    public BigDecimal getRedPacketAccountByUserId(long userId){
        return redPacketAccountService.getRedPacketAccountByUserId(userId);
    }

    public BigDecimal getCapitalAccountByUserId(long userId){
        return capitalAccountService.getCapitalAccountByUserId(userId);
    }
}
