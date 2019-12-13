package io.tyloo.tcctransaction.interceptor;

import io.tyloo.tcctransaction.common.MethodRole;
import io.tyloo.tcctransaction.support.FactoryBuilder;
import org.aspectj.lang.ProceedingJoinPoint;
import io.tyloo.api.Compensable;
import io.tyloo.api.Propagation;
import io.tyloo.api.TransactionContext;
import io.tyloo.api.UniqueIdentity;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/*
 * 注解方法上下文
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 15:33 2019/12/4
 *
 */
public class CompensableMethodContext {

    /**
     * 切入点
     */
    ProceedingJoinPoint pjp = null;

    /**
     * 注解方法
     */
    Method method = null;

    /**
     * 注解
     */
    Compensable compensable = null;

    /**
     * 传播级别
     */
    Propagation propagation = null;

    /**
     * 事务上下文
     */
    TransactionContext transactionContext = null;

    public CompensableMethodContext(ProceedingJoinPoint pjp) {
        this.pjp = pjp;
        this.method = getCompensableMethod();
        this.compensable = method.getAnnotation(Compensable.class);
        this.propagation = compensable.propagation();
        this.transactionContext = FactoryBuilder.factoryOf(compensable.transactionContextEditor()).getInstance().get(pjp.getTarget(), method, pjp.getArgs());

    }

    public Compensable getAnnotation() {
        return compensable;
    }

    public Propagation getPropagation() {
        return propagation;
    }

    public TransactionContext getTransactionContext() {
        return transactionContext;
    }

    public Method getMethod(ProceedingJoinPoint pjp) {
        return method;
    }

    /**
     * 获取唯一标识
     *
     * @return
     */
    public Object getUniqueIdentity() {
        Annotation[][] annotations = this.getMethod(pjp).getParameterAnnotations();

        for (int i = 0; i < annotations.length; i++) {
            for (Annotation annotation : annotations[i]) {
                if (annotation.annotationType().equals(UniqueIdentity.class)) {

                    Object[] params = pjp.getArgs();
                    Object unqiueIdentity = params[i];

                    return unqiueIdentity;
                }
            }
        }

        return null;
    }

    /**
     * 获取注解方法
     *
     * @return
     */
    private Method getCompensableMethod() {
        return getMethod(pjp);
    }

    /**
     * 通过该方法事务传播级别获取方法类型
     *
     *
     * @param propagation
     * @param isTransactionActive
     * @param transactionContext
     * @return
     */
    public MethodRole getMethodRole(Propagation propagation, boolean isTransactionActive, TransactionContext transactionContext) {
        return getMethodRole(this.propagation, isTransactionActive, this.transactionContext);
    }

    public Object proceed() throws Throwable {
        return this.pjp.proceed();
    }
}