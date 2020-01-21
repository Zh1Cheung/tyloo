package io.tyloo.tcctransaction.unittest.service;

import io.tyloo.api.Tyloo;
import io.tyloo.api.Propagation;
import io.tyloo.api.TylooContext;
import io.tyloo.tcctransaction.unittest.utils.UnitTest;
import io.tyloo.tcctransaction.unittest.client.AccountRecordServiceProxy;
import io.tyloo.tcctransaction.unittest.entity.AccountStatus;
import io.tyloo.tcctransaction.unittest.entity.SubAccount;
import io.tyloo.tcctransaction.unittest.repository.SubAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/*
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 8:34 2019/12/5
 *
 */
@Service
public class AccountServiceImpl implements AccountService {


    @Autowired
    AccountRecordServiceProxy accountRecordServiceProxy;

    @Autowired
    SubAccountRepository subAccountRepository;

    @Override
    @Tyloo(confirmMethod = "transferFromConfirm", cancelMethod = "transferFromCancel")
    public void transferFrom(TransactionContext transactionContext, long accountId, int amount) {
        System.out.println("transferFrom called");
        SubAccount subAccount = subAccountRepository.findById(accountId);
        subAccount.setStatus(AccountStatus.TRANSFERING.getId());
        subAccount.setBalanceAmount(subAccount.getBalanceAmount() - amount);
        accountRecordServiceProxy.record(null, accountId, amount);
    }

    /**
     * 向子账户转账，状态为“转账中：TRANSFERING”
     */
    @Override
    @Tyloo(propagation = Propagation.REQUIRED, confirmMethod = "transferToConfirm", cancelMethod = "transferToCancel")
    public void transferTo(TylooContext transactionContext, long accountId, int amount) {

        System.out.println("transferTo called");
        SubAccount subAccount = subAccountRepository.findById(accountId);
        subAccount.setStatus(AccountStatus.TRANSFERING.getId());
        subAccount.setBalanceAmount(subAccount.getBalanceAmount() + amount);
    }

    @Override
    @Tyloo(confirmMethod = "transferFromConfirm", cancelMethod = "transferFromCancel")
    public void transferFromWithMultipleTier(TylooContext transactionContext, long accountId, int amount) {
        System.out.println("transferFromWithMultipleTier called");
        SubAccount subAccount = subAccountRepository.findById(accountId);
        subAccount.setStatus(AccountStatus.TRANSFERING.getId());
        subAccount.setBalanceAmount(subAccount.getBalanceAmount() + amount);
        accountRecordServiceProxy.record(null, accountId, amount);
    }

    @Override
    @Tyloo(confirmMethod = "transferToConfirm", cancelMethod = "transferToCancel")
    public void transferToWithMultipleTier(TylooContext transactionContext, long accountId, int amount) {

        System.out.println("transferToWithMultipleTier called");

        SubAccount subAccount = subAccountRepository.findById(accountId);
        subAccount.setStatus(AccountStatus.TRANSFERING.getId());
        subAccount.setBalanceAmount(subAccount.getBalanceAmount() + amount);

        accountRecordServiceProxy.record(null, accountId, amount);
    }


    @Override
    @Tyloo(propagation = Propagation.REQUIRES_NEW, confirmMethod = "transferToConfirmWithNoTransactionContext", cancelMethod = "transferToCancelWithNoTransactionContext")
    public void transferToWithNoTransactionContext(long accountId, int amount) {

        System.out.println("transferToWithNoTransactionContext called");
        SubAccount subAccount = subAccountRepository.findById(accountId);
        subAccount.setStatus(AccountStatus.TRANSFERING.getId());
        subAccount.setBalanceAmount(subAccount.getBalanceAmount() + amount);
        accountRecordServiceProxy.record(null, accountId, amount);
    }


    public void transferFromConfirm(TylooContext transactionContext, long accountId, int amount) {
        System.out.println("transferFromConfirm called");
        SubAccount subAccount = subAccountRepository.findById(accountId);
        subAccount.setStatus(AccountStatus.NORMAL.getId());
    }

    public void transferFromCancel(TylooContext transactionContext, long accountId, int amount) {
        System.out.println("transferFromCancel called");
        SubAccount subAccount = subAccountRepository.findById(accountId);

        if (subAccount.getStatus() == AccountStatus.TRANSFERING.getId()) {

            subAccount.setStatus(AccountStatus.NORMAL.getId());
            subAccount.setBalanceAmount(subAccount.getBalanceAmount() + amount);
        }
    }

    /**
     * 向子账户转账确认，状态改为“常规状态：NORMAL”
            */
    public void transferToConfirm(TylooContext transactionContext, long accountId, int amount) {
        System.out.println("transferToConfirm called");

        if (UnitTest.CONFIRMING_EXCEPTION) {
            throw new RuntimeException("transferToConfirm confirm failed.");
        }

        SubAccount subAccount = subAccountRepository.findById(accountId);
        subAccount.setStatus(AccountStatus.NORMAL.getId());
    }

    /**
     * 向子账户转账取消，状态改为“常规状态：NORMAL”
     */
    public void transferToCancel(TylooContext transactionContext, long accountId, int amount) {
        System.out.println("transferToCancel called");

        SubAccount subAccount = subAccountRepository.findById(accountId);

        if (subAccount.getStatus() == AccountStatus.TRANSFERING.getId()) {

            subAccount.setStatus(AccountStatus.NORMAL.getId());
            subAccount.setBalanceAmount(subAccount.getBalanceAmount() - amount);
        }
    }

    public void transferToConfirmWithNoTransactionContext(long accountId, int amount) {
        System.out.println("transferToConfirmWithNoTransactionContext called");
        SubAccount subAccount = subAccountRepository.findById(accountId);
        subAccount.setStatus(AccountStatus.NORMAL.getId());
    }

    public void transferToCancelWithNoTransactionContext(long accountId, int amount) {
        System.out.println("transferToCancelWithNoTransactionContext called");

        SubAccount subAccount = subAccountRepository.findById(accountId);

        if (subAccount.getStatus() == AccountStatus.TRANSFERING.getId()) {

            subAccount.setStatus(AccountStatus.NORMAL.getId());
            subAccount.setBalanceAmount(subAccount.getBalanceAmount() - amount);
        }
    }

}
