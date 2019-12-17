package io.tyloo.tcctransaction.sample.dubbo.order.service;

import io.tyloo.tcctransaction.sample.dubbo.capital.api.CapitalAccountService;
import io.tyloo.tcctransaction.sample.dubbo.redpacket.api.RedPacketAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/*
 *
 * 账户实现类
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 9:08 2019/12/5
 *
 */
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
