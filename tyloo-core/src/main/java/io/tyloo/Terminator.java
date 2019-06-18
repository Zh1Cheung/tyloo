package io.tyloo;

import io.tyloo.api.TransactionContext;
import io.tyloo.api.TransactionContextEditor;
import io.tyloo.support.FactoryBuilder;
import io.tyloo.utils.StringUtils;

import java.lang.reflect.Method;

/*
 *
 * 执行器
 *
 * @Author:Zh1Cheung zh1cheunglq@gmail.com
 * @Date: 20:01 2019/6/6
 *
 */

public final class Terminator {

    public Terminator() {

    }

    /**
     * 根据调用上下文，获取目标方法并执行方法调用.
     *
     * @param invocationContext
     */
    public static void invoke(TransactionContext transactionContext, InvocationContext invocationContext, Class<? extends TransactionContextEditor> transactionContextEditorClass) {


        if (StringUtils.isNotEmpty(invocationContext.getMethodName())) {

            try {

                Object target = FactoryBuilder.factoryOf(invocationContext.getTargetClass()).getInstance();

                Method method = null;

                //注入事务上下文
                method = target.getClass().getMethod(invocationContext.getMethodName(), invocationContext.getParameterTypes());

                FactoryBuilder.factoryOf(transactionContextEditorClass).getInstance().set(transactionContext, target, method, invocationContext.getArgs());

                // 调用服务方法，被再次被TylooAspect和TylooCoordinatorAspect拦截，但因为事务状态已经不再是TRYING了，所以直接执行远程服务
                method.invoke(target, invocationContext.getArgs());

            } catch (Exception e) {
                throw new SystemException(e);
            }
        }
    }
}
