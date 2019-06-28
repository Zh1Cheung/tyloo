package io.tyloo;

import io.tyloo.api.TransactionContext;
import io.tyloo.api.TransactionStatus;
import io.tyloo.common.TransactionType;
import org.apache.log4j.Logger;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;

/*
 *
 * 事务管理器
 *
 * @Author:Zh1Cheung zh1cheunglq@gmail.com
 * @Date: 20:25 2019/6/7
 *
 */

public class TransactionManager {

    static final Logger logger = Logger.getLogger(TransactionManager.class.getSimpleName());

    private TransactionRepository transactionRepository;

    /**
     * 当前线程事务队列
     */
    private static final ThreadLocal<Deque<Transaction>> CURRENT = new ThreadLocal<Deque<Transaction>>();

    private ExecutorService executorService;

    public void setTransactionRepository(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public TransactionManager() {


    }

    /**
     *
     * 发起根事务
     * 完成 begin 方法之后，其实也就创建完了一个根环境的全局事务管理器，这个根环境其实就是order订单环境
     * 接着回到 rootMethodProceed 方法继续往下执行
     *
     * @param uniqueIdentify
     * @return
     */
    public Transaction begin(Object uniqueIdentify) {
        Transaction transaction = new Transaction(uniqueIdentify, TransactionType.ROOT);
        transactionRepository.create(transaction);
        registerTransaction(transaction);
        return transaction;
    }


    /**
     * 传播发起分支事务
     *
     * @param transactionContext 事务上下文
     * @return 分支事务
     */
    public Transaction propagationNewBegin(TransactionContext transactionContext) {

        Transaction transaction = new Transaction(transactionContext);
        transactionRepository.create(transaction);

        registerTransaction(transaction);
        return transaction;
    }

    /**
     * 传播获取分支事务
     *
     * @param transactionContext 事务上下文
     * @return 分支事务
     * @throws NoExistedTransactionException 当事务不存在时
     */
    public Transaction propagationExistBegin(TransactionContext transactionContext) throws NoExistedTransactionException {
        Transaction transaction = null;
        try {
            transaction = transactionRepository.findByXid(transactionContext.getXid());
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        if (transaction != null) {
            transaction.changeStatus(TransactionStatus.valueOf(transactionContext.getStatus()));
            registerTransaction(transaction);
            return transaction;
        } else {
            throw new NoExistedTransactionException();
        }
    }

    public void commit(boolean asyncCommit) {

        final Transaction transaction = getCurrentTransaction();

        transaction.changeStatus(TransactionStatus.CONFIRMING);

        transactionRepository.update(transaction);

        if (asyncCommit) {
            try {
                Long statTime = System.currentTimeMillis();

                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        commitTransaction(transaction);
                    }
                });
                logger.debug("async submit cost time:" + (System.currentTimeMillis() - statTime));
            } catch (Throwable commitException) {
                logger.warn("tyloo transaction async submit confirm failed, recovery job will try to confirm later.", commitException);
                throw new ConfirmingException(commitException);
            }
        } else {
            commitTransaction(transaction);
        }
    }


    public void rollback(boolean asyncRollback) {

        final Transaction transaction = getCurrentTransaction();
        transaction.changeStatus(TransactionStatus.CANCELLING);

        transactionRepository.update(transaction);

        if (asyncRollback) {

            try {
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        rollbackTransaction(transaction);
                    }
                });
            } catch (Throwable rollbackException) {
                logger.warn("tyloo transaction async rollback failed, recovery job will try to rollback later.", rollbackException);
                throw new CancellingException(rollbackException);
            }
        } else {

            rollbackTransaction(transaction);
        }
    }


    private void commitTransaction(Transaction transaction) {
        try {
            transaction.commit();
            transactionRepository.delete(transaction);
        } catch (Throwable commitException) {
            logger.warn("tyloo transaction confirm failed, recovery job will try to confirm later.", commitException);
            throw new ConfirmingException(commitException);
        }
    }

    private void rollbackTransaction(Transaction transaction) {
        try {
            transaction.rollback();
            transactionRepository.delete(transaction);
        } catch (Throwable rollbackException) {
            logger.warn("tyloo transaction rollback failed, recovery job will try to rollback later.", rollbackException);
            throw new CancellingException(rollbackException);
        }
    }

    /**
     * 获取当前线程事务第一个(头部)元素
     *
     * @return 事务
     */
    public Transaction getCurrentTransaction() {
        if (isTransactionActive()) {
            return CURRENT.get().peek();
        }
        return null;
    }

    public boolean isTransactionActive() {
        Deque<Transaction> transactions = CURRENT.get();
        return transactions != null && !transactions.isEmpty();
    }

    /**
     * 注册事务到当前线程事务队列
     *
     * @param transaction 事务
     */
    private void registerTransaction(Transaction transaction) {

        if (CURRENT.get() == null) {
            CURRENT.set(new LinkedList<Transaction>());
        }

        CURRENT.get().push(transaction);
    }

    /**
     * 将事务从当前线程事务队列移除
     *
     * @param transaction 事务
     */
    public void cleanAfterCompletion(Transaction transaction) {
        if (isTransactionActive() && transaction != null) {
            Transaction currentTransaction = getCurrentTransaction();
            if (currentTransaction == transaction) {
                CURRENT.get().pop();
                if (CURRENT.get().size() == 0) {
                    CURRENT.remove();
                }
            } else {
                throw new SystemException("Illegal transaction when clean after completion");
            }
        }
    }

    /**
     * 添加参与者到事务
     *
     * @param participant 参与者
     */
    public void enlistParticipant(Participant participant) {
        Transaction transaction = this.getCurrentTransaction();
        transaction.enlistParticipant(participant);
        transactionRepository.update(transaction);
    }
}
