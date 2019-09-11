package io.tyloo.sample.http.redpacket.service;

import io.tyloo.sample.http.redpacket.api.RedPacketAccountService;
import io.tyloo.sample.redpacket.domain.repository.RedPacketAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;


public class RedPacketAccountServiceImpl implements RedPacketAccountService {

    @Autowired
    RedPacketAccountRepository redPacketAccountRepository;

    @Override
    public BigDecimal getRedPacketAccountByUserId(long userId) {
        return redPacketAccountRepository.findByUserId(userId).getBalanceAmount();
    }
}
