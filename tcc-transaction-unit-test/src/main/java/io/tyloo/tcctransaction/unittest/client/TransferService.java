package io.tyloo.tcctransaction.unittest.client;

import io.tyloo.api.Compensable;
import io.tyloo.api.Propagation;
import io.tyloo.api.UniqueIdentity;
import io.tyloo.tcctransaction.unittest.repository.SubAccountRepository;
import io.tyloo.tcctransaction.unittest.entity.AccountStatus;
import io.tyloo.tcctransaction.unittest.entity.SubAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
 *
 * 转账服务
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 8:27 2019/12/5
 *
 */
@Service
public class TransferService {

    @Autowired
    AccountServiceProxy accountService;

    @Autowired
    SubAccountRepository subAccountRepository;

    public TransferService() {
    }


    @Compensable
    @Transactional
    public void performenceTuningTransfer() {
        accountService.performanceTuningTransferTo(null);
    }

    @Compensable(propagation = Propagation.MANDATORY)
    public void transferWithMandatoryPropagation(long fromAccountId, long toAccountId, int amount) {
        System.out.println("transfer called");

        SubAccount subAccount = subAccountRepository.findById(fromAccountId);

        subAccount.setStatus(AccountStatus.TRANSFERING.getId());

        subAccount.setBalanceAmount(subAccount.getBalanceAmount() - amount);

        accountService.transferTo(null, toAccountId, amount);
    }

    @Compensable(confirmMethod = "transferConfirm", cancelMethod = "transferCancel")
    @Transactional
    public void transfer(@UniqueIdentity long fromAccountId, long toAccountId, int amount) {

        System.out.println("transfer called");

        SubAccount subAccount = subAccountRepository.findById(fromAccountId);

        subAccount.setStatus(AccountStatus.TRANSFERING.getId());

        subAccount.setBalanceAmount(subAccount.getBalanceAmount() - amount);

        accountService.transferTo(null, toAccountId, amount);
    }

    @Compensable(confirmMethod = "transferConfirm", cancelMethod = "transferCancel")
    public void transferWithMultipleTier(long fromAccountId, long toAccountId, int amount) {

        System.out.println("transferWithMultipleTier called");

        SubAccount subAccount = subAccountRepository.findById(fromAccountId);

        subAccount.setStatus(AccountStatus.TRANSFERING.getId());

        subAccount.setBalanceAmount(subAccount.getBalanceAmount() - amount);

        accountService.transferToWithMultipleTier(null, toAccountId, amount);
    }

    @Compensable(confirmMethod = "transferWithMultipleConsumerConfirm", cancelMethod = "transferWithMultipleConsumerCancel")
    @Transactional
    public void transferWithMultipleConsumer(long fromAccountId, long toAccountId, int amount) {
        System.out.println("transferWithMultipleConsumer called");
        accountService.transferFrom(null, fromAccountId, amount);
        accountService.transferTo(null, toAccountId, amount);
    }

    @Compensable
    public void transferWithOnlyTryAndMultipleConsumer(long fromAccountId, long toAccountId, int amount) {
        System.out.println("transferWithOnlyTryAndMultipleConsumer called");
        accountService.transferFrom(null, fromAccountId, amount);
        accountService.transferTo(null, toAccountId, amount);
    }

    @Compensable
    public void transferWithNoTransactionContext(long fromAccountId, long toAccountId, int amount) {
        System.out.println("transferWithNoTransactionContext called");
        accountService.transferTo(toAccountId, amount);
    }

    public void transferConfirm(long fromAccountId, long toAccountId, int amount) {
        System.out.println("transferConfirm called");
        SubAccount subAccount = subAccountRepository.findById(fromAccountId);
        subAccount.setStatus(AccountStatus.NORMAL.getId());
    }

    public void transferCancel(long fromAccountId, long toAccountId, int amount) {

        System.out.println("transferCancel called");

        SubAccount subAccount = subAccountRepository.findById(fromAccountId);

        if (subAccount.getStatus() == AccountStatus.TRANSFERING.getId()) {
            subAccount.setBalanceAmount(subAccount.getBalanceAmount() + amount);
        }

        subAccount.setStatus(AccountStatus.NORMAL.getId());
    }

    public void transferWithMultipleConsumerConfirm(long fromAccountId, long toAccountId, int amount) {
        System.out.println("transferWithMultipleConsumerConfirm called");
    }

    public void transferWithMultipleConsumerCancel(long fromAccountId, long toAccountId, int amount) {
        System.out.println("transferWithMultipleConsumerCancel called");
    }
}
