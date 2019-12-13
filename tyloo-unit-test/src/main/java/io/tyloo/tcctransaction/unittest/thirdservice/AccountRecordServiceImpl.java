package io.tyloo.tcctransaction.unittest.thirdservice;

import io.tyloo.api.TransactionContext;
import io.tyloo.api.Compensable;
import io.tyloo.tcctransaction.unittest.repository.AccountRecordRepository;
import io.tyloo.tcctransaction.unittest.utils.UnitTest;
import io.tyloo.tcctransaction.unittest.entity.AccountRecord;
import io.tyloo.tcctransaction.unittest.entity.AccountStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/*
 *
 * 账户记录服务接口实现
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 8:36 2019/12/5
 *
 */
@Service
public class AccountRecordServiceImpl implements AccountRecordService {

    @Autowired
    AccountRecordRepository accountRecordRepository;

    @Compensable(confirmMethod = "recordConfirm", cancelMethod = "recordCancel")
    public void record(TransactionContext transactionContext, long accountId, int amount) {

        System.out.println("record");

        AccountRecord accountRecord = accountRecordRepository.findById(accountId);
        accountRecord.setBalanceAmount(amount);
        accountRecord.setStatusId(AccountStatus.TRANSFERING.getId());

        if (UnitTest.TRYING_EXCEPTION) {
            throw new RuntimeException("record try failed.");
        }
    }

    public void recordConfirm(TransactionContext transactionContext, long accountId, int amount) {
        System.out.println("recordConfirm");
        AccountRecord accountRecord = accountRecordRepository.findById(accountId);
        accountRecord.setStatusId(AccountStatus.NORMAL.getId());
    }

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
