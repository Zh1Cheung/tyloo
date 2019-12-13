package io.tyloo.tcctransaction.recover;

import com.alibaba.fastjson.JSON;
import io.tyloo.tcctransaction.exception.OptimisticLockException;
import io.tyloo.tcctransaction.Transaction;
import io.tyloo.tcctransaction.common.TransactionType;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import io.tyloo.tcctransaction.TransactionRepository;
import io.tyloo.api.TransactionStatus;
import io.tyloo.tcctransaction.support.TransactionConfigurator;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/*
 *
 * 事务恢复
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 18:51 2019/12/4
 *
 */
public class TransactionRecovery {

    static final Logger logger = Logger.getLogger(TransactionRecovery.class.getSimpleName());

    /**
     * TCC事务配置器.
     */
    private TransactionConfigurator transactionConfigurator;

    /**
     * 启动事务恢复操作(被RecoverScheduledJob定时任务调用).
     */
    public void startRecover() {

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
        // 获取事务库
        TransactionRepository transactionRepository = transactionConfigurator.getTransactionRepository();
        // 获取事务恢复配置
        RecoverConfig recoverConfig = transactionConfigurator.getRecoverConfig();

        return transactionRepository.findAllUnmodifiedSince(new Date(currentTimeInMillis - recoverConfig.getRecoverDuration() * 1000));
    }

    /**
     * 恢复错误的事务.
     *
     * @param transactions
     */
    private void recoverErrorTransactions(List<Transaction> transactions) {


        for (Transaction transaction : transactions) {

            if (transaction.getRetriedCount() > transactionConfigurator.getRecoverConfig().getMaxRetryCount()) {

                logger.error(String.format("recover failed with max retry count,will not try again. txid:%s, status:%s,retried count:%d,transaction content:%s", transaction.getXid(), transaction.getStatus().getId(), transaction.getRetriedCount(), JSON.toJSONString(transaction)));
                continue;
            }

            if (transaction.getTransactionType().equals(TransactionType.BRANCH)
                    && (transaction.getCreateTime().getTime() +
                    transactionConfigurator.getRecoverConfig().getMaxRetryCount() *
                            transactionConfigurator.getRecoverConfig().getRecoverDuration() * 1000
                    > System.currentTimeMillis())) {
                continue;
            }

            try {
                // 重试次数+1
                transaction.addRetriedCount();

                if (transaction.getStatus().equals(TransactionStatus.CONFIRMING)) {
                    // 如果是CONFIRMING(2)状态，则将事务往前执行
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

    /**
     * 设置事务配置器.
     *
     * @param transactionConfigurator
     */
    public void setTransactionConfigurator(TransactionConfigurator transactionConfigurator) {
        this.transactionConfigurator = transactionConfigurator;
    }
}
