package io.tyloo.unittest.client;

import io.tyloo.api.TransactionContext;
import io.tyloo.unittest.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;


@Service
public class AccountServiceProxy {

    @Autowired
    AccountService accountService;

    private ExecutorService executorService = Executors.newFixedThreadPool(100);

    public void transferFromWithMultipleTier(final TransactionContext transactionContext, final long accountId, final int amount) {
        Future<Boolean> future = this.executorService
                .submit(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        accountService.transferFromWithMultipleTier(transactionContext, accountId, amount);
                        return true;
                    }
                });

        handleResult(future);
    }

    public void transferToWithMultipleTier(final TransactionContext transactionContext, final long accountId, final int amount) {
//        Future<Boolean> future = this.executorService
//                .submit(new Callable<Boolean>() {
//                    @Override
//                    public Boolean call() throws Exception {
//                        accountService.transferToWithMultipleTier(transactionContext, accountId, amount);
//                        return true;
//                    }
//                });
//
//        handleResult(future);
        accountService.transferToWithMultipleTier(transactionContext, accountId, amount);
    }

    public void performanceTuningTransferTo(TransactionContext transactionContext) {
    }

    public void transferTo(final TransactionContext transactionContext, final long accountId, final int amount) {

//        Future<Boolean> future = this.executorService
//                .submit(new Callable<Boolean>() {
//                    @Override
//                    public Boolean call() throws Exception {
//                        accountService.transferTo(transactionContext, accountId, amount);
//                        return true;
//                    }
//                });
//
//        handleResult(future);
        accountService.transferTo(transactionContext, accountId, amount);
    }

    public void transferTo(final long accountId, final int amount) {

//        Future<Boolean> future = this.executorService
//                .submit(new Callable<Boolean>() {
//                    @Override
//                    public Boolean call() throws Exception {
//                        accountService.transferToWithNoTransactionContext(accountId, amount);
//                        return true;
//                    }
//                });
//
//        handleResult(future);

        accountService.transferToWithNoTransactionContext(accountId, amount);
    }

    public void transferFrom(final TransactionContext transactionContext, final long accountId, final int amount) {

//        Future<Boolean> future = this.executorService
//                .submit(new Callable<Boolean>() {
//                    @Override
//                    public Boolean call() throws Exception {
//                        accountService.transferFrom(transactionContext, accountId, amount);
//                        return true;
//                    }
//                });
//
//        handleResult(future);

        accountService.transferFrom(transactionContext, accountId, amount);
    }


    private void handleResult(Future<Boolean> future) {
        while (!future.isDone()) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            future.get();
        } catch (InterruptedException e) {
            throw new Error(e);
        } catch (ExecutionException e) {
            throw new Error(e);
        }
    }


}
