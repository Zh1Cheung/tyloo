package io.tyloo.dubbo.proxy.jdk;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.proxy.InvokerInvocationHandler;
import io.tyloo.api.Enums.Propagation;
import io.tyloo.api.Annotation.Tyloo;
import io.tyloo.dubbo.context.DubboTransactionTransactionContextLoader;
import io.tyloo.core.interceptor.TylooCoordinatorAspect;
import io.tyloo.core.support.FactoryBuilder;
import io.tyloo.core.utils.ReflectionUtils;
import org.aspectj.lang.ProceedingJoinPoint;

import java.lang.reflect.Method;

/*
 *
 * TCC 调用处理器
 * 在调用 Dubbo Service 服务时，使用 TylooCoordinatorAspect 拦截处理
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 10:55 2019/12/5
 *
 */
public class TccInvokerInvocationHandler extends InvokerInvocationHandler {

    private Object target;

    public TccInvokerInvocationHandler(Invoker<?> handler) {
        super(handler);
    }

    public <T> TccInvokerInvocationHandler(T target, Invoker<T> invoker) {
        super(invoker);
        this.target = target;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Tyloo tyloo = method.getAnnotation(Tyloo.class);

        if (tyloo != null) {
            // 设置 @Tyloo 属性
            if (StringUtils.isEmpty(tyloo.confirmMethod())) {
                ReflectionUtils.changeAnnotationValue(tyloo, "confirmMethod", method.getName());
                ReflectionUtils.changeAnnotationValue(tyloo, "cancelMethod", method.getName());
                ReflectionUtils.changeAnnotationValue(tyloo, "tylooContextLoader", DubboTransactionTransactionContextLoader.class);
                ReflectionUtils.changeAnnotationValue(tyloo, "propagation", Propagation.SUPPORTS);
            }

            /**
             * 生成方法切面
             * 调用 TylooCoordinatorAspect#interceptTransactionContextMethod 方法，对方法切面拦截处理。
             * 为什么无需调用 TylooTransactionAspect 切面？
             * 因为传播级别为 Propagation.SUPPORTS，不会发起事务。
             */
            ProceedingJoinPoint pjp = new MethodProceedingJoinPoint(proxy, target, method, args);
            TylooCoordinatorAspect tylooCoordinatorAspect = (TylooCoordinatorAspect) FactoryBuilder.factoryOf(TylooCoordinatorAspect.class).getInstance();
            return tylooCoordinatorAspect.interceptTransactionContextMethod(pjp);

        } else {
            return super.invoke(target, method, args);
        }
    }


}
