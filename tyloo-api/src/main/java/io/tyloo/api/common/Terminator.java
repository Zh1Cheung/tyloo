package io.tyloo.api.common;


import io.tyloo.api.Context.InvocationContext;
import io.tyloo.api.Context.TylooTransactionContext;
import io.tyloo.api.Context.TylooTransactionContextLoader;
import io.tyloo.core.exception.SystemException;
import io.tyloo.core.support.FactoryBuilder;
import io.tyloo.core.utils.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@Data
@AllArgsConstructor
@NoArgsConstructor
class Terminator implements Serializable {

    private static final long serialVersionUID = -164958655471605778L;

    /**
     * 根据调用上下文，获取目标方法并执行方法调用.
     *
     * @param invocationContext
     */

    public void invoke(TylooTransactionContext tylooTransactionContext, InvocationContext invocationContext, Class<? extends TylooTransactionContextLoader> tylooContextLoaderClass) {


        if (StringUtils.isNotEmpty(invocationContext.getMethodName())) {

            try {
                //获得目标类的一个实例（对象）
                Object target = FactoryBuilder.factoryOf(invocationContext.getTargetClass()).getInstance();

                Method method = null;
                // 找到要调用的目标方法
                method = target.getClass().getMethod(invocationContext.getMethodName(), invocationContext.getParameterTypes());

                //注入事务上下文
                TylooTransactionContextLoader instance = (TylooTransactionContextLoader) FactoryBuilder.factoryOf(tylooContextLoaderClass).getInstance();
                instance.set(tylooTransactionContext, target, method, invocationContext.getArgs());

                // 调用服务方法，被再次被TylooAspect和TylooCoordinatorAspect拦截，但因为事务状态已经不再是TRYING了，所以直接执行远程服务
                method.invoke(target, invocationContext.getArgs());

            } catch (Exception e) {
                throw new SystemException(e);
            }
        }
    }
}
