package io.tyloo.sample.redpacket.domain.repository;


import io.tyloo.sample.exception.InsufficientBalanceException;
import io.tyloo.sample.redpacket.domain.entity.RedPacketAccount;
import io.tyloo.sample.redpacket.infrastructure.dao.RedPacketAccountDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


@Repository
public class RedPacketAccountRepository {

    @Autowired
    RedPacketAccountDao redPacketAccountDao;

    public RedPacketAccount findByUserId(long userId) {

        return redPacketAccountDao.findByUserId(userId);
    }

    public void save(RedPacketAccount redPacketAccount) {
        int effectCount = redPacketAccountDao.update(redPacketAccount);
        if (effectCount < 1) {
            throw new InsufficientBalanceException();
        }
    }
}
