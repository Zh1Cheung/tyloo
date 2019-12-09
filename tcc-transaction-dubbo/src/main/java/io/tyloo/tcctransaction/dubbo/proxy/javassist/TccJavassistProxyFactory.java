package io.tyloo.tcctransaction.dubbo.proxy.javassist;

import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.proxy.InvokerInvocationHandler;
import com.alibaba.dubbo.rpc.proxy.javassist.JavassistProxyFactory;

/*
 *
 * Javassist 是一个开源的分析、编辑和创建 Java 字节码的类库。通过使用Javassist 对字节码操作可以实现动态 ”AOP” 框架。
 * 基于 Javassist 方式
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 9:33 2019/12/5
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
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Invoker<T> invoker, Class<?>[] interfaces) {
        return (T) TccProxy.getProxy(interfaces).newInstance(new InvokerInvocationHandler(invoker));
    }
}
