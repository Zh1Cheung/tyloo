package io.tyloo.core.interceptor;

import io.tyloo.api.Annotation.Tyloo;
import io.tyloo.api.Annotation.UniqueIdentity;
import io.tyloo.api.Context.TylooTransactionContext;
import io.tyloo.api.Context.TylooTransactionContextLoader;
import io.tyloo.api.Enums.Propagation;
import io.tyloo.api.Enums.Role;
import io.tyloo.core.support.FactoryBuilder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/*
 * 注解方法上下文
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 15:33 2019/12/4
 *
 */
public class TylooMethodContext {

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
    Tyloo tyloo = null;

    /**
     * 传播级别
     */
    Propagation propagation = null;

    /**
     * 事务上下文
     */
    TylooTransactionContext tylooTransactionContext = null;

    public TylooMethodContext(ProceedingJoinPoint pjp) {
        this.pjp = pjp;
        this.method = getTylooMethod();
        this.tyloo = method.getAnnotation(Tyloo.class);
        this.propagation = tyloo.propagation();
        TylooTransactionContextLoader instance = (TylooTransactionContextLoader) FactoryBuilder.factoryOf(tyloo.tylooContextLoader()).getInstance();
        this.tylooTransactionContext = instance.get(pjp.getTarget(), method, pjp.getArgs());

    }

    public Tyloo getAnnotation() {
        return tyloo;
    }

    public Propagation getPropagation() {
        return propagation;
    }

    public TylooTransactionContext getTylooTransactionContext() {
        return tylooTransactionContext;
    }

    public Method getMethod() {
        return method;
    }

    /**
     * 获取唯一标识
     *
     * @return
     */
    public Object getUniqueIdentity() {
        Annotation[][] annotations = this.getMethod().getParameterAnnotations();

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
    private Method getTylooMethod() {
        Method method = ((MethodSignature) (pjp.getSignature())).getMethod();

        if (method.getAnnotation(Tyloo.class) == null) {
            try {
                method = pjp.getTarget().getClass().getMethod(method.getName(), method.getParameterTypes());
            } catch (NoSuchMethodException e) {
                return null;
            }
        }
        return method;
    }

    /**
     * 通过该方法事务传播级别获取方法类型
     *
     * @param isTransactionActive
     * @return
     */
    public Role getMethodRole(boolean isTransactionActive) {
        if ((propagation.equals(Propagation.REQUIRED) && !isTransactionActive && tylooTransactionContext == null) ||
                propagation.equals(Propagation.REQUIRES_NEW)) {
            return Role.ROOT;
        } else if ((propagation.equals(Propagation.REQUIRED) || propagation.equals(Propagation.MANDATORY)) && !isTransactionActive && tylooTransactionContext != null) {
            return Role.PROVIDER;
        } else {
            return Role.NORMAL;
        }
    }

    public Object proceed() throws Throwable {
        return this.pjp.proceed();
    }
}