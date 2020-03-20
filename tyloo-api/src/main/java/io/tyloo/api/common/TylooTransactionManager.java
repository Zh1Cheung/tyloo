package io.tyloo.api.common;

import io.tyloo.api.Context.TylooTransactionContext;
import io.tyloo.api.Enums.TransactionStatus;
import io.tyloo.api.Enums.TransactionType;
import io.tyloo.core.exception.CancellingException;
import io.tyloo.core.exception.ConfirmingException;
import io.tyloo.core.exception.NoExistedTransactionException;
import io.tyloo.core.exception.SystemException;
import io.tyloo.core.repository.TransactionRepository;
import org.apache.log4j.Logger;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;


/*
 *
 * 事务管理器
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 20:25 2019/12/4
 *
 */
public class TylooTransactionManager {

    static final Logger logger = Logger.getLogger(TylooTransactionManager.class.getSimpleName());
    /**
     * 事务存储器
     */
    private TransactionRepository transactionRepository;
    /**
     * 当前线程事务队列
     */
    private static final ThreadLocal<Deque<TylooTransaction>> CURRENT = new ThreadLocal<Deque<TylooTransaction>>();

    private ExecutorService executorService;

    public void setTransactionRepository(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public TylooTransactionManager() {


    }

    /**
     * 完成 begin 方法之后，其实也就创建完了一个根环境的全局事务管理器，这个根环境其实就是order订单环境
     * 接着回到 rootMethodProceed 方法继续往下执行
     *
     * @param uniqueIdentify
     * @return
     */
    public TylooTransaction begin(Object uniqueIdentify) {
        TylooTransaction tylooTransaction = new TylooTransaction(uniqueIdentify, TransactionType.ROOT);
        transactionRepository.create(tylooTransaction);
        registerTransaction(tylooTransaction);
        return tylooTransaction;
    }

    /**
     * 发起根事务
     *
     * @return 事务
     */
    public TylooTransaction begin() {
        TylooTransaction tylooTransaction = new TylooTransaction(TransactionType.ROOT);
        transactionRepository.create(tylooTransaction);
        registerTransaction(tylooTransaction);
        return tylooTransaction;
    }

    /**
     * 传播发起分支事务
     *
     * @param tylooTransactionContext 事务上下文
     * @return 分支事务
     */
    public TylooTransaction propagationNewBegin(TylooTransactionContext tylooTransactionContext) {

        TylooTransaction tylooTransaction = new TylooTransaction(tylooTransactionContext);
        transactionRepository.create(tylooTransaction);

        registerTransaction(tylooTransaction);
        return tylooTransaction;
    }

    /**
     * 传播获取分支事务
     *
     * @param tylooTransactionContext 事务上下文
     * @return 分支事务
     * @throws NoExistedTransactionException 当事务不存在时
     */
    public TylooTransaction propagationExistBegin(TylooTransactionContext tylooTransactionContext) throws NoExistedTransactionException {
        // 查询 事务
        TylooTransaction tylooTransaction = transactionRepository.findByXid(tylooTransactionContext.getXid());

        if (tylooTransaction != null) {
            // 设置 事务 状态
            tylooTransaction.setTransactionStatus(TransactionStatus.valueOf(tylooTransactionContext.getStatus()));
            // 注册 事务
            registerTransaction(tylooTransaction);
            return tylooTransaction;
        } else {
            throw new NoExistedTransactionException();
        }
    }

    /**
     * 提交事务
     */
    public void commit(boolean asyncCommit) {
        // 获取 事务
        final TylooTransaction tylooTransaction = getCurrentTransaction();
        // 设置 事务状态 为 CONFIRMING
        tylooTransaction.setTransactionStatus(TransactionStatus.CONFIRMING);
        // 更新 事务
        transactionRepository.update(tylooTransaction);

        if (asyncCommit) {
            try {
                Long statTime = System.currentTimeMillis();

                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        commitTransaction(tylooTransaction);
                    }
                });
                logger.debug("async submit cost time:" + (System.currentTimeMillis() - statTime));
            } catch (Throwable commitException) {
                logger.warn("Tyloo tylooTransaction async submit confirm failed, recovery job will try to confirm later.", commitException);
                throw new ConfirmingException(commitException);
            }
        } else {
            commitTransaction(tylooTransaction);
        }
    }

    /**
     * 回滚事务
     */
    public void rollback(boolean asyncRollback) {

        final TylooTransaction tylooTransaction = getCurrentTransaction();
        tylooTransaction.setTransactionStatus(TransactionStatus.CANCELLING);

        transactionRepository.update(tylooTransaction);

        if (asyncRollback) {

            try {
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        rollbackTransaction(tylooTransaction);
                    }
                });
            } catch (Throwable rollbackException) {
                logger.warn("Tyloo tylooTransaction async rollback failed, recovery job will try to rollback later.", rollbackException);
                throw new CancellingException(rollbackException);
            }
        } else {

            rollbackTransaction(tylooTransaction);
        }
    }


    private void commitTransaction(TylooTransaction tylooTransaction) {
        try {
            // 提交 事务
            tylooTransaction.commit();
            // 删除 事务
            transactionRepository.delete(tylooTransaction);
        } catch (Throwable commitException) {
            logger.warn("Tyloo tylooTransaction confirm failed, recovery job will try to confirm later.", commitException);
            throw new ConfirmingException(commitException);
        }
    }

    private void rollbackTransaction(TylooTransaction tylooTransaction) {
        try {
            // 回退 事务
            tylooTransaction.rollback();
            // 删除 事务
            transactionRepository.delete(tylooTransaction);
        } catch (Throwable rollbackException) {
            logger.warn("Tyloo tylooTransaction rollback failed, recovery job will try to rollback later.", rollbackException);
            throw new CancellingException(rollbackException);
        }
    }

    /**
     * 获取当前线程事务第一个(头部)元素
     *
     * @return 事务
     */
    public TylooTransaction getCurrentTransaction() {
        if (isTransactionActive()) {
            return CURRENT.get().peek();
        }
        return null;
    }

    /**
     * 当前线程是否在事务中
     *
     * @return 是否在事务中
     */
    public boolean isTransactionActive() {
        Deque<TylooTransaction> tylooTransactions = CURRENT.get();
        return tylooTransactions != null && !tylooTransactions.isEmpty();
    }

    /**
     * 注册事务到当前线程事务队列
     *
     * @param tylooTransaction 事务
     */
    private void registerTransaction(TylooTransaction tylooTransaction) {

        if (CURRENT.get() == null) {
            CURRENT.set(new LinkedList<TylooTransaction>());
        }

        CURRENT.get().push(tylooTransaction);
    }

    /**
     * 将事务从当前线程事务队列移除
     *
     * @param tylooTransaction 事务
     */
    public void cleanAfterCompletion(TylooTransaction tylooTransaction) {
        if (isTransactionActive() && tylooTransaction != null) {
            TylooTransaction currentTylooTransaction = getCurrentTransaction();
            if (currentTylooTransaction == tylooTransaction) {
                CURRENT.get().pop();
                if (CURRENT.get().size() == 0) {
                    CURRENT.remove();
                }
            } else {
                throw new SystemException("Illegal tylooTransaction when clean after completion");
            }
        }
    }

    /**
     * 添加参与者到事务
     *
     * @param participant 参与者
     */
    public void enlistParticipant(Participant participant) {
        TylooTransaction tylooTransaction = this.getCurrentTransaction();
        tylooTransaction.enlistParticipant(participant);
        transactionRepository.update(tylooTransaction);
    }
}
