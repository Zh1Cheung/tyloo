package io.tyloo.unittest.client;

import io.tyloo.api.TransactionContext;
import io.tyloo.unittest.thirdservice.AccountRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.util.concurrent.Executors.*;


@Service
public class AccountRecordServiceProxy {


    @Autowired
    private AccountRecordService accountRecordService;

    private ExecutorService executorService = newFixedThreadPool(100);

    public void record(final TransactionContext transactionContext, final long accountId, final int amount) {
//        Future<Boolean> future = this.executorService
//                .submit(new Callable<Boolean>() {
//                    @Override
//                    public Boolean call() throws Exception {
//                        accountRecordService.record(transactionContext, accountId, amount);
//                        return true;
//                    }
//                });
//
//        handleResult(future);

        accountRecordService.record(transactionContext, accountId, amount);

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
        } catch (InterruptedException | ExecutionException e) {
            throw new Error(e);
        }
    }
}
