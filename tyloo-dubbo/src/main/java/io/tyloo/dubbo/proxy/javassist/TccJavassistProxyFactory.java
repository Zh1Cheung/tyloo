package io.tyloo.dubbo.proxy.javassist;

import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.proxy.InvokerInvocationHandler;
import org.apache.dubbo.rpc.proxy.javassist.JavassistProxyFactory;

/*
 *
 * Javassist 是一个开源的分析、编辑和创建 Java 字节码的类库。通过使用Javassist 对字节码操作可以实现动态 ”AOP” 框架。
 * 基于 Javassist 方式
 * @Author:Zh1Cheung zh1cheunglq@gmail.com
 * @Date: 9:33 2019/8/16
 *
 */
public class TccJavassistProxyFactory extends JavassistProxyFactory {

    /**
     * 项目启动时，调用getProxy(...)` 方法，生成 Dubbo Service 调用 Proxy。
     * com.alibaba.dubbo.rpc.proxy.InvokerInvocationHandler`，Dubbo 调用处理器
     *
     * @param invoker
     * @param interfaces
     * @param <T>
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Invoker<T> invoker, Class<?>[] interfaces) {
        return (T) TccProxy.getProxy(interfaces).newInstance(new InvokerInvocationHandler(invoker));
    }
}
