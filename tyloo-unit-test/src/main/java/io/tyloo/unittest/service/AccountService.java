package io.tyloo.unittest.service;

import io.tyloo.api.TransactionContext;


public interface AccountService {

    void transferTo(TransactionContext transactionContext, long accountId, int amount);

    void transferToConfirm(TransactionContext transactionContext, long accountId, int amount);

    void transferToCancel(TransactionContext transactionContext, long accountId, int amount);

    void transferToWithNoTransactionContext(long accountId, int amount);

    void transferToConfirmWithNoTransactionContext(long accountId, int amount);

    void transferToCancelWithNoTransactionContext(long accountId, int amount);

    void transferFrom(TransactionContext transactionContext, long accountId, int amount);

    void transferFromConfirm(TransactionContext transactionContext, long accountId, int amount);

    void transferFromCancel(TransactionContext transactionContext, long accountId, int amount);

    void transferToWithMultipleTier(TransactionContext transactionContext, long accountId, int amount);

    void transferFromWithMultipleTier(TransactionContext transactionContext, long accountId, int amount);
}
