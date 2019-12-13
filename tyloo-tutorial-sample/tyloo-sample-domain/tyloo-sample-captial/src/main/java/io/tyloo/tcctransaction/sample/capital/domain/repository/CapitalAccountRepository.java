package io.tyloo.tcctransaction.sample.capital.domain.repository;

import io.tyloo.tcctransaction.sample.capital.domain.entity.CapitalAccount;
import io.tyloo.tcctransaction.sample.exception.InsufficientBalanceException;
import io.tyloo.tcctransaction.sample.capital.infrastructure.dao.CapitalAccountDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/*
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 9:29 2019/12/5
 *
 */
@Repository
public class CapitalAccountRepository {

    @Autowired
    CapitalAccountDao capitalAccountDao;

    public CapitalAccount findByUserId(long userId) {

        return capitalAccountDao.findByUserId(userId);
    }

    public void save(CapitalAccount capitalAccount) {
        int effectCount = capitalAccountDao.update(capitalAccount);
        if (effectCount < 1) {
            throw new InsufficientBalanceException();
        }
    }
}
