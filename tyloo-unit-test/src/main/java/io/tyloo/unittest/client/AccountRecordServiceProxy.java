package io.tyloo.unittest.client;

import io.tyloo.unittest.thirdservice.AccountRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

/*
 *
 * 账户记录服务代理
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 8:24 2019/12/5
 *
 */
@Service
public class AccountRecordServiceProxy {


    @Autowired
    private AccountRecordService accountRecordService;

    private ExecutorService executorService = Executors.newFixedThreadPool(100);

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
        } catch (InterruptedException e) {
            throw new Error(e);
        } catch (ExecutionException e) {
            throw new Error(e);
        }
    }
}
