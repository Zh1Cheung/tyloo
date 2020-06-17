package io.tyloo.recover;

import com.alibaba.fastjson.JSON;
import io.tyloo.OptimisticLockException;
import io.tyloo.Transaction;
import io.tyloo.TransactionRepository;
import io.tyloo.api.TransactionStatus;
import io.tyloo.common.TransactionType;
import io.tyloo.support.TransactionConfigurator;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/*
 *
 * 事务恢复
 *
 * 事务信息被持久化到外部的存储器（事务库）中。事务存储是事务恢复的基础。通过读取外部存储器中的异常事务，定时任务会按照一定频率对事务进行重试，直到事务完成或超过最大重试次数。
 *
 * @Author:Zh1Cheung zh1cheunglq@gmail.com
 * @Date: 18:51 2019/5/2
 *
 */
public class TylooTransactionRecovery {

    static final Logger logger = Logger.getLogger(TylooTransactionRecovery.class.getSimpleName());

    /**
     * TCC事务配置器.
     */
    private TransactionConfigurator transactionConfigurator;

    /**
     * 启动事务恢复操作(被RecoverScheduledJob定时任务调用).
     */
    public void startRecover() throws CloneNotSupportedException {

        List<Transaction> transactions = loadErrorTransactions();

        recoverErrorTransactions(transactions);
    }

    /**
     * 找出所有执行错误的事务信息
     *
     * @return
     */
    private List<Transaction> loadErrorTransactions() {


        long currentTimeInMillis = Calendar.getInstance().getTimeInMillis();

        TransactionRepository transactionRepository = transactionConfigurator.getTransactionRepository();
        TylooRecoverConfiguration tylooRecoverConfiguration = transactionConfigurator.getTylooRecoverConfiguration();

        return transactionRepository.findAllUnmodifiedSince(new Date(currentTimeInMillis - tylooRecoverConfiguration.getRecoverDuration() * 1000));
    }

    /**
     * 恢复错误的事务.
     *
     * @param transactions
     */
    private void recoverErrorTransactions(List<Transaction> transactions) throws CloneNotSupportedException {


        for (Transaction transaction : transactions) {

            //比较重试次数，大于则跳过该事务
            if (transaction.getRetriedCount() > transactionConfigurator.getTylooRecoverConfiguration().getMaxRetryCount()) {
                logger.error(String.format("recover failed with max retry count,will not try again. txid:%s, status:%s,retried count:%d,transaction content:%s", transaction.getXid(), transaction.getStatus().getId(), transaction.getRetriedCount(), JSON.toJSONString(transaction)));
                continue;
            }

            //当前事务是分支事务或超时，则跳过该事务
            if (transaction.getTransactionType().equals(TransactionType.BRANCH)
                    && (transaction.getCreateTime().getTime() +
                    transactionConfigurator.getTylooRecoverConfiguration().getMaxRetryCount() *
                            transactionConfigurator.getTylooRecoverConfiguration().getRecoverDuration() * 1000
                    > System.currentTimeMillis())) {
                continue;
            }

            try {
                transaction.addRetriedCount();
                // 如果是CONFIRMING(2)状态，则将事务往前执行
                if (transaction.getStatus().equals(TransactionStatus.CONFIRMING)) {

                    transaction.changeStatus(TransactionStatus.CONFIRMING);
                    transactionConfigurator.getTransactionRepository().update(transaction);
                    transaction.commit();
                    transactionConfigurator.getTransactionRepository().delete(transaction);

                } else if (transaction.getStatus().equals(TransactionStatus.CANCELLING)
                        || transaction.getTransactionType().equals(TransactionType.ROOT)) {
                    // 其他情况，把事务状态改为CANCELLING(3)，然后执行回滚
                    transaction.changeStatus(TransactionStatus.CANCELLING);
                    transactionConfigurator.getTransactionRepository().update(transaction);
                    transaction.rollback();
                    // 其他情况下，超时没处理的事务日志直接删除
                    transactionConfigurator.getTransactionRepository().delete(transaction);
                }

            } catch (Throwable throwable) {

                if (throwable instanceof OptimisticLockException
                        || ExceptionUtils.getRootCause(throwable) instanceof OptimisticLockException) {
                    logger.warn(String.format("optimisticLockException happened while recover. txid:%s, status:%s,retried count:%d,transaction content:%s", transaction.getXid(), transaction.getStatus().getId(), transaction.getRetriedCount(), JSON.toJSONString(transaction)), throwable);
                } else {
                    logger.error(String.format("recover failed, txid:%s, status:%s,retried count:%d,transaction content:%s", transaction.getXid(), transaction.getStatus().getId(), transaction.getRetriedCount(), JSON.toJSONString(transaction)), throwable);
                }
            }
        }
    }

    public void setTransactionConfigurator(TransactionConfigurator transactionConfigurator) {
        this.transactionConfigurator = transactionConfigurator;
    }
}
