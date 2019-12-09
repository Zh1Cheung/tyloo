package io.tyloo.tcctransaction.sample.dubbo.redpacket.service;

import io.tyloo.tcctransaction.sample.dubbo.redpacket.api.RedPacketAccountService;
import io.tyloo.tcctransaction.sample.redpacket.domain.repository.RedPacketAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/*
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 9:23 2019/12/5
 *
 */
@Service("redPacketAccountService")
public class RedPacketAccountServiceImpl implements RedPacketAccountService {

    @Autowired
    RedPacketAccountRepository redPacketAccountRepository;

    @Override
    public BigDecimal getRedPacketAccountByUserId(long userId) {
        return redPacketAccountRepository.findByUserId(userId).getBalanceAmount();
    }
}
