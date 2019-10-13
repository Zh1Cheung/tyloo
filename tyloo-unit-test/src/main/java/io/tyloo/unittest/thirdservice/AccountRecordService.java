package io.tyloo.unittest.thirdservice;

import io.tyloo.api.TransactionContext;


public interface AccountRecordService {
    public void record(TransactionContext transactionContext, long accountId, int amount);

    void recordConfirm(TransactionContext transactionContext, long accountId, int amount);

    void recordCancel(TransactionContext transactionContext, long accountId, int amount);
}
