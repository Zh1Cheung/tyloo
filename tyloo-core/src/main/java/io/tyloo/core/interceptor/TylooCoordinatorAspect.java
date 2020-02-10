package io.tyloo.core.interceptor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/*
 * 资源协调拦截器对应的切面
 * 通过@Pointcut + @Around 注解，配置对 @Tyloo 注解的方法进行拦截，
 * 调用 TylooInterceptor#interceptTylooMethod(...) 方法进行处理。
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 18:48 2019/12/4
 *
 */
@Aspect
public abstract class TylooCoordinatorAspect {

    private TylooCoordinatorInterceptor tylooCoordinatorInterceptor;

    @Pointcut("@annotation(io.tyloo.api.Annotation.Tyloo)")
    public void transactionContextCall() {

    }

    @Around("transactionContextCall()")
    public Object interceptTransactionContextMethod(ProceedingJoinPoint pjp) throws Throwable {
        return tylooCoordinatorInterceptor.interceptTransactionContextMethod(pjp);
    }

    public void setTylooCoordinatorInterceptor(TylooCoordinatorInterceptor tylooCoordinatorInterceptor) {
        this.tylooCoordinatorInterceptor = tylooCoordinatorInterceptor;
    }

    public abstract int getOrder();
}
