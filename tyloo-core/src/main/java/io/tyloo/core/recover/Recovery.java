package io.tyloo.core.recover;

import com.alibaba.fastjson.JSON;
import io.tyloo.api.common.TylooTransaction;
import io.tyloo.core.exception.OptimisticLockException;
import io.tyloo.api.Enums.TransactionType;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import io.tyloo.core.repository.TransactionRepository;
import io.tyloo.api.Enums.TransactionStatus;
import io.tyloo.core.support.TransactionConfigurator;

import java.util.Calendar;
import java.util.Date;
import java.util.List;


/*
 *
 * 事务恢复
 *
 * 事务信息被持久化到外部的存储器（事务库）中。事务存储是事务恢复的基础。通过读取外部存储器中的异常事务，定时任务会按照一定频率对事务进行重试，直到事务完成或超过最大重试次数。
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 18:51 2019/12/4
 *
 */
public class Recovery {

    static final Logger logger = Logger.getLogger(Recovery.class.getSimpleName());

    /**
     * TCC事务配置器.
     */
    private TransactionConfigurator transactionConfigurator;

    /**
     * 启动事务恢复操作(被RecoverScheduledJob定时任务调用).
     */
    public void startRecover() {

        List<TylooTransaction> tylooTransactions = loadErrorTransactions();

        recoverErrorTransactions(tylooTransactions);
    }

    /**
     * 找出所有执行错误的事务信息
     *
     * @return
     */
    private List<TylooTransaction> loadErrorTransactions() {


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
     * @param tylooTransactions
     */
    private void recoverErrorTransactions(List<TylooTransaction> tylooTransactions) {


        for (TylooTransaction tylooTransaction : tylooTransactions) {
            //比较重试次数，大于则跳过该事务
            if (tylooTransaction.getRetriedCount() > transactionConfigurator.getRecoverConfig().getMaxRetryCount()) {

                logger.error(String.format("recover failed with max retry count,will not try again. txid:%s, status:%s,retried count:%d,tylooTransaction content:%s", tylooTransaction.getXid(), tylooTransaction.getTransactionStatus().getId(), tylooTransaction.getRetriedCount(), JSON.toJSONString(tylooTransaction)));
                continue;
            }

            //当前事务是分支事务或超时，则跳过该事务
            if (tylooTransaction.getTransactionType().equals(TransactionType.BRANCH)
                    && (tylooTransaction.getCreateTime().getTime() +
                    transactionConfigurator.getRecoverConfig().getMaxRetryCount() *
                            transactionConfigurator.getRecoverConfig().getRecoverDuration() * 1000
                    > System.currentTimeMillis())) {
                continue;
            }

            try {
                // 重试次数+1
                tylooTransaction.addRetriedCount();

                // 如果是CONFIRMING(2)状态，则将事务往前执行
                if (tylooTransaction.getTransactionStatus().equals(TransactionStatus.CONFIRMING)) {

                    tylooTransaction.changeStatus(TransactionStatus.CONFIRMING);

                    transactionConfigurator.getTransactionRepository().update(tylooTransaction);
                    tylooTransaction.commit();
                    transactionConfigurator.getTransactionRepository().delete(tylooTransaction);

                } else if (tylooTransaction.getTransactionStatus().equals(TransactionStatus.CANCELLING)
                        || tylooTransaction.getTransactionType().equals(TransactionType.ROOT)) {
                    // 其他情况，把事务状态改为CANCELLING(3)，然后执行回滚
                    tylooTransaction.changeStatus(TransactionStatus.CANCELLING);
                    transactionConfigurator.getTransactionRepository().update(tylooTransaction);
                    tylooTransaction.rollback();
                    // 其他情况下，超时没处理的事务日志直接删除
                    transactionConfigurator.getTransactionRepository().delete(tylooTransaction);
                }

            } catch (Throwable throwable) {

                if (throwable instanceof OptimisticLockException
                        || ExceptionUtils.getRootCause(throwable) instanceof OptimisticLockException) {
                    logger.warn(String.format("optimisticLockException happened while recover. txid:%s, status:%s,retried count:%d,tylooTransaction content:%s", tylooTransaction.getXid(), tylooTransaction.getTransactionStatus().getId(), tylooTransaction.getRetriedCount(), JSON.toJSONString(tylooTransaction)), throwable);
                } else {
                    logger.error(String.format("recover failed, txid:%s, status:%s,retried count:%d,tylooTransaction content:%s", tylooTransaction.getXid(), tylooTransaction.getTransactionStatus().getId(), tylooTransaction.getRetriedCount(), JSON.toJSONString(tylooTransaction)), throwable);
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
