package io.tyloo.tcctransaction.interceptor;

import io.tyloo.api.Propagation;
import io.tyloo.api.TransactionContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/*
 * 可补偿事务拦截器对应的切面
 * 通过@Pointcut + @Around 注解，配置对 @Compensable 注解的方法进行拦截，
 * 调用 CompensableTransactionInterceptor#interceptCompensableMethod(...) 方法进行处理。
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 15:14 2019/12/4
 *
 */

@Aspect
public abstract class CompensableTransactionAspect {

    private CompensableTransactionInterceptor compensableTransactionInterceptor;

    public void setCompensableTransactionInterceptor(CompensableTransactionInterceptor compensableTransactionInterceptor) {
        this.compensableTransactionInterceptor = compensableTransactionInterceptor;
    }

    @Pointcut("@annotation(io.tyloo.api.Compensable)")
    public void compensableService() {

    }

    @Around("compensableService()")
    public Object interceptCompensableMethod(ProceedingJoinPoint pjp, Propagation propagation, TransactionContext transactionContext) throws Throwable {

        return compensableTransactionInterceptor.interceptCompensableMethod(pjp, propagation, transactionContext);
    }

    public abstract int getOrder();
}
