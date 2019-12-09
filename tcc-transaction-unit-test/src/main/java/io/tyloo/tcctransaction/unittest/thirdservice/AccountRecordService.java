package io.tyloo.tcctransaction.unittest.thirdservice;

import io.tyloo.api.TransactionContext;

/*
 *
 * 账户记录服务接口
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 8:36 2019/12/5
 *
 */
public interface AccountRecordService {
    public void record(TransactionContext transactionContext, long accountId, int amount);

    void recordConfirm(TransactionContext transactionContext, long accountId, int amount);

    void recordCancel(TransactionContext transactionContext, long accountId, int amount);
}
