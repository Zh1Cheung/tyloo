package io.tyloo.unittest.thirdservice;

import io.tyloo.api.Tyloo;
import io.tyloo.api.TransactionContext;
import io.tyloo.unittest.entity.AccountRecord;
import io.tyloo.unittest.entity.AccountStatus;
import io.tyloo.unittest.repository.AccountRecordRepository;
import io.tyloo.unittest.utils.UnitTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class AccountRecordServiceImpl implements AccountRecordService {

    @Autowired
    AccountRecordRepository accountRecordRepository;

    @Override
    @Tyloo(confirmMethod = "recordConfirm", cancelMethod = "recordCancel")
    public void record(TransactionContext transactionContext, long accountId, int amount) {

        System.out.println("record");

        AccountRecord accountRecord = accountRecordRepository.findById(accountId);
        accountRecord.setBalanceAmount(amount);
        accountRecord.setStatusId(AccountStatus.TRANSFERING.getId());

        if (UnitTest.TRYING_EXCEPTION) {
            throw new RuntimeException("record try failed.");
        }
    }

    @Override
    public void recordConfirm(TransactionContext transactionContext, long accountId, int amount) {
        System.out.println("recordConfirm");
        AccountRecord accountRecord = accountRecordRepository.findById(accountId);
        accountRecord.setStatusId(AccountStatus.NORMAL.getId());
    }

    @Override
    public void recordCancel(TransactionContext transactionContext, long accountId, int amount) {
        System.out.println("recordCancel");

        if (UnitTest.TRYING_EXCEPTION) {
            throw new RuntimeException("record cancel failed.");
        }

        AccountRecord accountRecord = accountRecordRepository.findById(accountId);
        accountRecord.setBalanceAmount(accountRecord.getBalanceAmount() - amount);
        accountRecord.setStatusId(AccountStatus.NORMAL.getId());


    }
}
