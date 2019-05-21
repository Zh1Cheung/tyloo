package io.tyloo.interceptor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/*
 * 可补偿事务拦截器对应的切面
 * 通过@Pointcut + @Around 注解，配置对 @Tyloo 注解的方法进行拦截
 *
 * @Author:Zh1Cheung zh1cheunglq@gmail.com
 * @Date: 15:14 2019/4/26
 *
 */
@Aspect
public abstract class TylooTransactionAspect {

    private TylooTransactionInterceptor tylooTransactionInterceptor;

    public void setTylooTransactionInterceptor(TylooTransactionInterceptor tylooTransactionInterceptor) {
        this.tylooTransactionInterceptor = tylooTransactionInterceptor;
    }

    @Pointcut("@annotation(io.tyloo.api.Tyloo)")
    public void tylooService() {

    }

    @Around("tylooService()")
    public Object interceptTylooMethod(ProceedingJoinPoint pjp) throws Throwable {

        return tylooTransactionInterceptor.interceptTylooMethod(pjp);
    }

    public abstract int getOrder();
}
