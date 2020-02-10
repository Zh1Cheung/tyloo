package io.tyloo.dubbo.proxy.jdk;

import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.proxy.InvokerInvocationHandler;
import com.alibaba.dubbo.rpc.proxy.jdk.JdkProxyFactory;

import java.lang.reflect.Proxy;


/*
 *
 * TCC JDK 代理工厂
 * 基于 JDK 动态代理机制
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 10:52 2019/12/5
 *
 */
public class TccJdkProxyFactory extends JdkProxyFactory {

    /**
     * - 项目启动时，调用 `TccJavassistProxyFactory#getProxy(...)` 方法，生成 Dubbo Service 调用 Proxy。
     * - 第一次调用 `Proxy#newProxyInstance(...)` 方法，创建调用 Dubbo Service 服务的 Proxy。
     * - 第二次调用 `Proxy#newProxyInstance(...)` 方法，创建对调用 Dubbo Service 的 Proxy 的 Proxy。
     *
     * @param invoker
     * @param interfaces
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Invoker<T> invoker, Class<?>[] interfaces) {

        T proxy = (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), interfaces, new InvokerInvocationHandler(invoker));

        T tccProxy = (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), interfaces, new TccInvokerInvocationHandler(proxy, invoker));

        return tccProxy;
    }
}