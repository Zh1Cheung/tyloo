package io.tyloo.tcctransaction.dubbo.proxy.jdk;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.proxy.InvokerInvocationHandler;
import io.tyloo.tcctransaction.dubbo.context.DubboTransactionContextEditor;
import io.tyloo.tcctransaction.interceptor.ResourceCoordinatorAspect;
import io.tyloo.tcctransaction.support.FactoryBuilder;
import io.tyloo.tcctransaction.utils.ReflectionUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import io.tyloo.api.Compensable;
import io.tyloo.api.Propagation;

import java.lang.reflect.Method;

/*
 *
 * TCC 调用处理器
 * 在调用 Dubbo Service 服务时，使用 ResourceCoordinatorInterceptor 拦截处理
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

        Compensable compensable = method.getAnnotation(Compensable.class);

        if (compensable != null) {
            // 设置 @Compensable 属性
            if (StringUtils.isEmpty(compensable.confirmMethod())) {
                ReflectionUtils.changeAnnotationValue(compensable, "confirmMethod", method.getName());
                ReflectionUtils.changeAnnotationValue(compensable, "cancelMethod", method.getName());
                ReflectionUtils.changeAnnotationValue(compensable, "transactionContextEditor", DubboTransactionContextEditor.class);
                ReflectionUtils.changeAnnotationValue(compensable, "propagation", Propagation.SUPPORTS);
            }

            /**
             * 生成方法切面
             * 调用 ResourceCoordinatorAspect#interceptTransactionContextMethod 方法，对方法切面拦截处理。
             * 为什么无需调用 CompensableTransactionAspect 切面？
             * 因为传播级别为 Propagation.SUPPORTS，不会发起事务。
             */
            ProceedingJoinPoint pjp = new MethodProceedingJoinPoint(proxy, target, method, args);
            return FactoryBuilder.factoryOf(ResourceCoordinatorAspect.class).getInstance().interceptTransactionContextMethod(pjp);
        } else {
            return super.invoke(target, method, args);
        }
    }


}
