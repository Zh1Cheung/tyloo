package io.tyloo.core.interceptor;

import com.alibaba.fastjson.JSON;
import io.tyloo.api.Enums.TransactionStatus;
import io.tyloo.api.common.TylooTransaction;
import io.tyloo.api.common.TylooTransactionManager;
import io.tyloo.core.exception.NoExistedTransactionException;
import io.tyloo.core.exception.SystemException;
import io.tyloo.core.utils.ReflectionUtils;
import io.tyloo.core.utils.TransactionUtils;
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
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 15:18 2019/12/4
 *
 */
public class TylooInterceptor {

    static final Logger logger = Logger.getLogger(TylooInterceptor.class.getSimpleName());

    /**
     * 事务配置器
     */
    private TylooTransactionManager tylooTransactionManager;

    private Set<Class<? extends Exception>> delayCancelExceptions = new HashSet<Class<? extends Exception>>();

    /**
     * 设置事务配置器.
     *
     * @param tylooTransactionManager
     */
    public void setTylooTransactionManager(TylooTransactionManager tylooTransactionManager) {
        this.tylooTransactionManager = tylooTransactionManager;
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

        // 从拦截方法的参数中获取事务上下文
        TylooMethodContext tylooMethodContext = new TylooMethodContext(pjp);

        boolean isTransactionActive = tylooTransactionManager.isTransactionActive();

        if (!TransactionUtils.isLegalTransactionContext(isTransactionActive, tylooMethodContext)) {
            throw new SystemException("no active tyloo transaction while propagation is mandatory for method " + tylooMethodContext.getMethod().getName());
        }

        // 计算可补偿事务方法类型
        switch (tylooMethodContext.getMethodRole(isTransactionActive)) {
            case ROOT:
                return rootMethodProceed(tylooMethodContext); // 主事务方法的处理
            case PROVIDER:
                return providerMethodProceed(tylooMethodContext); // 服务提供者事务方法处理
            default:
                return pjp.proceed(); // 其他的方法都是直接执行
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

        TylooTransaction tylooTransaction = null;

        boolean asyncConfirm = tylooMethodContext.getAnnotation().asyncConfirm();

        boolean asyncCancel = tylooMethodContext.getAnnotation().asyncCancel();

        Set<Class<? extends Exception>> allDelayCancelExceptions = new HashSet<Class<? extends Exception>>();
        allDelayCancelExceptions.addAll(this.delayCancelExceptions);
        allDelayCancelExceptions.addAll(Arrays.asList(tylooMethodContext.getAnnotation().delayCancelExceptions()));

        try {

            // 事务开始（创建事务日志记录，并在当前线程缓存该事务日志记录）
            tylooTransaction = tylooTransactionManager.begin(tylooMethodContext.getUniqueIdentity());

            try {
                // Try (开始执行被拦截的方法)
                returnValue = tylooMethodContext.proceed();
            } catch (Throwable tryingException) {

                if (!isDelayCancelException(tryingException, allDelayCancelExceptions)) {

                    logger.warn(String.format("tyloo tylooTransaction trying failed. tylooTransaction content:%s", JSON.toJSONString(tylooTransaction)), tryingException);

                    tylooTransactionManager.rollback(asyncCancel);
                }

                throw tryingException;
            }

            // Try检验正常后提交(事务管理器在控制提交)
            tylooTransactionManager.commit(asyncConfirm);

        } finally {
            tylooTransactionManager.cleanAfterCompletion(tylooTransaction);
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

        TylooTransaction tylooTransaction = null;


        boolean asyncConfirm = tylooMethodContext.getAnnotation().asyncConfirm();

        boolean asyncCancel = tylooMethodContext.getAnnotation().asyncCancel();

        try {

            switch (TransactionStatus.valueOf(tylooMethodContext.getTylooTransactionContext().getStatus())) {
                case TRYING:
                    // 基于全局事务ID扩展创建新的分支事务，并存于当前线程的事务局部变量中.
                    tylooTransaction = tylooTransactionManager.propagationNewBegin(tylooMethodContext.getTylooTransactionContext());
                    return tylooMethodContext.proceed();
                case CONFIRMING:
                    try {
                        // 找出存在的事务并处理.
                        tylooTransaction = tylooTransactionManager.propagationExistBegin(tylooMethodContext.getTylooTransactionContext());
                        // 提交
                        tylooTransactionManager.commit(asyncConfirm);
                    } catch (NoExistedTransactionException excepton) {
                        //the tylooTransaction has been commit,ignore it.
                    }
                    break;
                case CANCELLING:

                    try {
                        tylooTransaction = tylooTransactionManager.propagationExistBegin(tylooMethodContext.getTylooTransactionContext());
                        // 回滚
                        tylooTransactionManager.rollback(asyncCancel);
                    } catch (NoExistedTransactionException exception) {
                        //the tylooTransaction has been rollback,ignore it.
                    }
                    break;
            }

        } finally {
            tylooTransactionManager.cleanAfterCompletion(tylooTransaction);
        }

        Method method = tylooMethodContext.getMethod();

        return ReflectionUtils.getNullValue(method.getReturnType());
    }

    private boolean isDelayCancelException(Throwable throwable, Set<Class<? extends Exception>> delayCancelExceptions) {

        if (delayCancelExceptions != null) {
            for (Class delayCancelException : delayCancelExceptions) {

                Throwable rootCause = ExceptionUtils.getRootCause(throwable);

                if (delayCancelException.isAssignableFrom(throwable.getClass())
                        || (rootCause != null && delayCancelException.isAssignableFrom(rootCause.getClass()))) {
                    return true;
                }
            }
        }

        return false;
    }

}
