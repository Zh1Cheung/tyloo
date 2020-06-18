package io.tyloo.dubbo.proxy.jdk;

import io.tyloo.api.Propagation;
import io.tyloo.api.Tyloo;
import io.tyloo.dubbo.context.DubboTransactionContextEditor;
import io.tyloo.interceptor.TylooCoordinatorAspect;
import io.tyloo.support.FactoryBuilder;
import io.tyloo.utils.ReflectionUtils;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.proxy.InvokerInvocationHandler;
import org.aspectj.lang.ProceedingJoinPoint;

import java.lang.reflect.Method;

/*
 *
 * TCC 调用处理器
 * 在调用 Dubbo Service 服务时，使用 TylooCoordinatorAspect 拦截处理
 *
 * @Author:Zh1Cheung zh1cheunglq@gmail.com
 * @Date: 10:55 2019/9/23
 *
 */

public class TylooInvokerInvocationHandler extends InvokerInvocationHandler {

    private Object target;

    public TylooInvokerInvocationHandler(Invoker<?> handler) {
        super(handler);
    }

    public <T> TylooInvokerInvocationHandler(T target, Invoker<T> invoker) {
        super(invoker);
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Tyloo tyloo = method.getAnnotation(Tyloo.class);

        if (tyloo != null) {

            if (StringUtils.isEmpty(tyloo.confirmMethod())) {
                ReflectionUtils.changeAnnotationValue(tyloo, "confirmMethod", method.getName());
                ReflectionUtils.changeAnnotationValue(tyloo, "cancelMethod", method.getName());
                ReflectionUtils.changeAnnotationValue(tyloo, "transactionContextEditor", DubboTransactionContextEditor.class);
                ReflectionUtils.changeAnnotationValue(tyloo, "propagation", Propagation.SUPPORTS);
            }

            /**
             * 生成方法切面
             * 调用 TylooCoordinatorAspect#interceptTransactionContextMethod 方法，对方法切面拦截处理。
             * 为什么无需调用 TylooTransactionAspect 切面？
             * 因为传播级别为 Propagation.SUPPORTS，不会发起事务。
             */
            ProceedingJoinPoint pjp = new MethodProceedingJoinPoint(proxy, target, method, args);
            return FactoryBuilder.factoryOf(TylooCoordinatorAspect.class).getInstance().interceptTransactionContextMethod(pjp);
        } else {
            return super.invoke(target, method, args);
        }
    }


}
