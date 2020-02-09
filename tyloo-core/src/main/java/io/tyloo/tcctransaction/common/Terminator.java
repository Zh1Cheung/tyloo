package io.tyloo.tcctransaction.common;


import io.tyloo.api.Context.TylooContext;
import io.tyloo.api.Context.TylooContextLoader;
import io.tyloo.api.Context.InvocationContext;
import io.tyloo.tcctransaction.exception.SystemException;
import io.tyloo.tcctransaction.support.FactoryBuilder;
import io.tyloo.tcctransaction.utils.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Method;

/*
 *
 * 执行器
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 20:01 2019/12/4
 *
 */
public class Terminator implements Serializable {

    private static final long serialVersionUID = -164958655471605778L;


    public Terminator() {

    }

    /**
     * 根据调用上下文，获取目标方法并执行方法调用.
     *
     * @param invocationContext
     * @return
     */

    public Object invoke(TylooContext tylooContext, InvocationContext invocationContext, Class<? extends TylooContextLoader> tylooContextLoaderClass) {


        if (StringUtils.isNotEmpty(invocationContext.getMethodName())) {

            try {
                //获得目标类的一个实例（对象）
                Object target = FactoryBuilder.factoryOf(invocationContext.getTargetClass()).getInstance();

                Method method = null;
                // 找到要调用的目标方法
                method = target.getClass().getMethod(invocationContext.getMethodName(), invocationContext.getParameterTypes());

                //注入事务上下文
                TylooContextLoader instance = (TylooContextLoader) FactoryBuilder.factoryOf(tylooContextLoaderClass).getInstance();
                instance.set(tylooContext, target, method, invocationContext.getArgs());

                // 调用服务方法，被再次被TccTransactionContextAspect和ResourceCoordinatorInterceptor拦截，但因为事务状态已经不再是TRYING了，所以直接执行远程服务
                return method.invoke(target, invocationContext.getArgs());

            } catch (Exception e) {
                throw new SystemException(e);
            }
        }
        return null;
    }
}
