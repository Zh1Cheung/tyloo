package io.tyloo.tcctransaction.unittest.repository;

import io.tyloo.tcctransaction.unittest.entity.AccountRecord;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

/*
 *
 * 账户记录库
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 8:33 2019/12/5
 *
 */
@Repository
public class AccountRecordRepository {

    private Map<Long, AccountRecord> accountRecordMap = new HashMap<Long, AccountRecord>();

    /* 初始化3个账户：余额为0 **/ {
        accountRecordMap.put(1L, new AccountRecord(1, 0));
        accountRecordMap.put(2L, new AccountRecord(2, 0));
        accountRecordMap.put(3L, new AccountRecord(3, 0));
    }

    /**
     * 根据ID获取账户信息
     **/
    public AccountRecord findById(Long id) {
        return accountRecordMap.get(id);
    }
}
