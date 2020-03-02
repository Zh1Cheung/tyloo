package io.tyloo.core.utils;

import io.tyloo.api.Context.TylooContext;
import io.tyloo.api.Context.TylooTransactionContext;
import io.tyloo.api.Enums.MethodRole;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import io.tyloo.api.Annotation.Tyloo;
import io.tyloo.api.Enums.Propagation;

import java.lang.reflect.Method;

/*
 *
 * 注解方法工具类
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 19:50 2019/12/4
 *
 */
public class CompensableMethodUtils {
    /**
     * 获得带 @Tyloo 注解的方法
     *
     * @param pjp 切面点
     * @return 方法
     */
    public static Method getCompensableMethod(ProceedingJoinPoint pjp) {
        return getMethod(pjp);
    }

    public static Method getMethod(ProceedingJoinPoint pjp) {
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
     * 计算方法类型
     *
     * @param propagation         传播级别
     * @param isTransactionActive 是否事务开启
     * @param tylooContext  事务上下文
     * @return 方法类型
     */
    public static MethodRole calculateMethodType(Propagation propagation, boolean isTransactionActive, TylooTransactionContext tylooContext) {
        // Propagation.REQUIRED：支持当前事务，当前没有事务，就新建一个事务。
        return getMethodRole(propagation, isTransactionActive, tylooContext);
    }

    public static MethodRole getMethodRole(Propagation propagation, boolean isTransactionActive, TylooTransactionContext tylooContext) {
        if ((propagation.equals(Propagation.REQUIRED) && !isTransactionActive && tylooContext == null) ||
                propagation.equals(Propagation.REQUIRES_NEW)) {
            // Propagation.REQUIRES_NEW：新建事务，如果当前存在事务，把当前事务挂起。
            return MethodRole.ROOT;
        } else if ((propagation.equals(Propagation.REQUIRED) || propagation.equals(Propagation.MANDATORY)) && !isTransactionActive && tylooContext != null) {
            // Propagation.REQUIRED：支持当前事务
            return MethodRole.PROVIDER;
        } else {
            // Propagation.MANDATORY：支持当前事务
            return MethodRole.NORMAL;
        }
    }

    public static int getTransactionContextParamPosition(Class<?>[] parameterTypes) {

        int position = -1;

        for (int i = 0; i < parameterTypes.length; i++) {
            if (parameterTypes[i].equals(TylooTransactionContext.class)) {
                position = i;
                break;
            }
        }
        return position;
    }
}
