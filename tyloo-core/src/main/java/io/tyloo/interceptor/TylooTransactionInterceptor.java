package io.tyloo.interceptor;

import com.alibaba.fastjson.JSON;
import io.tyloo.NoExistedTransactionException;
import io.tyloo.SystemException;
import io.tyloo.Transaction;
import io.tyloo.TransactionManager;
import io.tyloo.api.TransactionStatus;
import io.tyloo.utils.ReflectionUtils;
import io.tyloo.utils.TransactionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/*
 * 可补偿事务拦截器。
 *
 * @Author:Zh1Cheung zh1cheunglq@gmail.com
 * @Date: 15:18 2019/4/23
 *
 */
public class TylooTransactionInterceptor {

    static final Logger logger = Logger.getLogger(TylooTransactionInterceptor.class.getSimpleName());
    private final Set<Class<? extends Exception>> delayCancelExceptions = new HashSet<>();
    private TransactionManager transactionManager;

    /**
     * 设置事务配置器.
     *
     * @param transactionManager
     */
    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void setDelayCancelExceptions(Set<Class<? extends Exception>> delayCancelExceptions) {
        this.delayCancelExceptions.addAll(delayCancelExceptions);
    }

    /**
     * 拦截补偿方法.
     *
     * @param pjp
     * @throws Throwable
     */
    public Object interceptTylooMethod(ProceedingJoinPoint pjp) throws Throwable {

        TylooMethodContext tylooMethodContext = new TylooMethodContext(pjp);
        boolean isTransactionActive = transactionManager.isTransactionActive();
        if (!TransactionUtils.isLegalTransactionContext(isTransactionActive, tylooMethodContext)) {
            throw new SystemException("no active tyloo transaction while propagation is mandatory for method " + tylooMethodContext.getMethod().getName());
        }

        switch (tylooMethodContext.getMethodRole(isTransactionActive)) {
            case ROOT:
                return rootMethodProceed(tylooMethodContext);
            case PROVIDER:
                return providerMethodProceed(tylooMethodContext);
            default:
                return pjp.proceed();
        }
    }

    /**
     * 主事务方法的处理.
     *
     * @param tylooMethodContext
     * @throws Throwable
     */
    private Object rootMethodProceed(TylooMethodContext tylooMethodContext) throws Throwable {

        Object returnValue = null;
        Transaction transaction = null;

        boolean asyncConfirm = tylooMethodContext.getAnnotation().asyncConfirm();
        boolean asyncCancel = tylooMethodContext.getAnnotation().asyncCancel();

        Set<Class<? extends Exception>> allDelayCancelExceptions = new HashSet<>();
        allDelayCancelExceptions.addAll(this.delayCancelExceptions);
        allDelayCancelExceptions.addAll(Arrays.asList(tylooMethodContext.getAnnotation().delayCancelExceptions()));

        try {
            transaction = transactionManager.begin(tylooMethodContext.getUniqueIdentity());
            try {
                returnValue = tylooMethodContext.proceed();
            } catch (Throwable tryingException) {
                if (!isDelayCancelException(tryingException, allDelayCancelExceptions)) {
                    logger.warn(String.format("tyloo transaction trying failed. transaction content:%s", JSON.toJSONString(transaction)), tryingException);
                    transactionManager.rollback(asyncCancel);
                }
                throw tryingException;
            }
            transactionManager.commit(asyncConfirm);
        } finally {
            transactionManager.cleanAfterCompletion(transaction);
        }
        return returnValue;
    }

    /**
     * 服务提供者事务方法处理.
     * 根据事务的状态是 CONFIRMING / CANCELLING 来调用对应方法
     *
     * @param tylooMethodContext
     * @throws Throwable
     */
    private Object providerMethodProceed(TylooMethodContext tylooMethodContext) throws Throwable {

        Transaction transaction = null;
        boolean asyncConfirm = tylooMethodContext.getAnnotation().asyncConfirm();
        boolean asyncCancel = tylooMethodContext.getAnnotation().asyncCancel();

        try {
            switch (TransactionStatus.valueOf(tylooMethodContext.getTransactionContext().getStatus())) {
                case TRYING:
                    transaction = transactionManager.propagationNewBegin(tylooMethodContext.getTransactionContext());
                    return tylooMethodContext.proceed();
                case CONFIRMING:
                    try {
                        transaction = transactionManager.propagationExistBegin(tylooMethodContext.getTransactionContext());
                        transactionManager.commit(asyncConfirm);
                    } catch (NoExistedTransactionException excepton) {
                        //the transaction has been commit,ignore it.
                    }
                    break;
                case CANCELLING:

                    try {
                        transaction = transactionManager.propagationExistBegin(tylooMethodContext.getTransactionContext());
                        transactionManager.rollback(asyncCancel);
                    } catch (NoExistedTransactionException exception) {
                        //the transaction has been rollback,ignore it.
                    }
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + TransactionStatus.valueOf(tylooMethodContext.getTransactionContext().getStatus()));
            }

        } finally {
            transactionManager.cleanAfterCompletion(transaction);
        }
        Method method = tylooMethodContext.getMethod();
        return ReflectionUtils.getNullValue(method.getReturnType());
    }

    private boolean isDelayCancelException(Throwable throwable, Set<Class<? extends Exception>> delayCancelExceptions) {

        if (delayCancelExceptions != null) {
            for (Class delayCancelException : delayCancelExceptions) {
                Throwable rootCause = ExceptionUtils.getRootCause(throwable);
                if (!throwable.getClass().isAssignableFrom(delayCancelException)) {
                    if ((rootCause != null) && rootCause.getClass().isAssignableFrom(delayCancelException)) {
                        return true;
                    }
                } else {
                    return true;
                }
            }
        }
        return false;
    }
}
