package io.tyloo.tcctransaction.unittest.repository;

import io.tyloo.tcctransaction.unittest.entity.SubAccount;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

/*
 *
 * 子账户库
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 8:33 2019/12/5
 *
 */
@Repository
public class SubAccountRepository {

    private Map<Long, SubAccount> subAccountMap = new HashMap<Long, SubAccount>();

    /* 初始化3个子账户：余额为0 **/ {
        subAccountMap.put(1L, new SubAccount(1, 100));
        subAccountMap.put(2L, new SubAccount(2, 200));
        subAccountMap.put(3L, new SubAccount(3, 300));
    }

    /**
     * 根据ID获取子账户信息
     **/
    public SubAccount findById(Long id) {
        return subAccountMap.get(id);
    }
}
