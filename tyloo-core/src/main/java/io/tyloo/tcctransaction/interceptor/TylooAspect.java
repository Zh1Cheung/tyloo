package io.tyloo.tcctransaction.interceptor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/*
 * 可补偿事务拦截器对应的切面
 * 通过@Pointcut + @Around 注解，配置对 @Tyloo 注解的方法进行拦截，
 * 调用 TylooInterceptor#interceptTylooMethod(...) 方法进行处理。
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 15:14 2019/12/4
 *
 */

@Aspect
public abstract class TylooAspect {

    private TylooInterceptor tylooInterceptor;


    public void setTylooInterceptor(TylooInterceptor tylooInterceptor) {
        this.tylooInterceptor = tylooInterceptor;
    }

    @Pointcut("@annotation(io.tyloo.api.Annotation.Tyloo)")
    public void tylooService() {

    }

    @Around("tylooService()")
    public Object interceptTylooMethod(ProceedingJoinPoint pjp) throws Throwable {

        return tylooInterceptor.interceptTylooMethod(pjp);
    }

    public abstract int getOrder();
}
