/*
 * Copyright 1999-2011 Alibaba Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.tyloo.tcctransaction.dubbo.proxy.javassist;

import com.alibaba.dubbo.common.utils.ClassHelper;
import com.alibaba.dubbo.common.utils.ReflectUtils;
import io.tyloo.api.Compensable;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/*
 *
 * TCC Proxy 工厂，生成 Dubbo Service 调用 Proxy
 *
 * 参考了 Dubbo 自带的实现：
 *     com.alibaba.dubbo.common.bytecode.Proxy
 *     com.alibaba.dubbo.common.bytecode.ClassGenerator
 *     com.alibaba.dubbo.common.bytecode.Wrapper
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 9:34 2019/12/5
 *
 */

public abstract class TccProxy {
    private static final AtomicLong PROXY_CLASS_COUNTER = new AtomicLong(0);

    private static final String PACKAGE_NAME = TccProxy.class.getPackage().getName();

    public static final InvocationHandler RETURN_NULL_INVOKER = new InvocationHandler() {
        public Object invoke(Object proxy, Method method, Object[] args) {
            return null;
        }
    };

    public static final InvocationHandler THROW_UNSUPPORTED_INVOKER = new InvocationHandler() {
        public Object invoke(Object proxy, Method method, Object[] args) {
            throw new UnsupportedOperationException("Method [" + ReflectUtils.getName(method) + "] unimplemented.");
        }
    };

    private static final Map<ClassLoader, Map<String, Object>> ProxyCacheMap = new WeakHashMap<ClassLoader, Map<String, Object>>();

    /**
     * 挂起生成标记
     */
    private static final Object PendingGenerationMarker = new Object();

    /**
     * TCC Proxy 工厂
     *
     * @param ics interface class array.
     * @return TccProxy instance.
     */
    public static TccProxy getProxy(Class<?>... ics) {
        return getProxy(ClassHelper.getCallerClassLoader(TccProxy.class), ics);
    }


    /**
     * 1. 校验接口
     * 2. 使用接口集合类名以 `;` 分隔拼接，作为 Proxy 的唯一标识
     * 3. 获得 Proxy 对应的 ClassLoader
     * 4. 一直获得 TCC Proxy 工厂直到成功。
     * 5. 生成 Dubbo Service 调用 ProxyFactory、Proxy 的代码生成器
     * 6. 生成 Dubbo Service 调用 Proxy 的代码。
     *
     * @param cl
     * @param ics
     * @return
     */
    public static TccProxy getProxy(ClassLoader cl, Class<?>... ics) {
        // 校验接口超过上限
        if (ics.length > 65535)
            throw new IllegalArgumentException("interface limit exceeded");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ics.length; i++) {
            String itf = ics[i].getName();
            // 校验是否为接口
            if (!ics[i].isInterface())
                throw new RuntimeException(itf + " is not a interface.");
            // 加载接口类
            Class<?> tmp = null;
            try {
                tmp = Class.forName(itf, false, cl);
            } catch (ClassNotFoundException e) {
            }

            if (tmp != ics[i])
                // 加载接口类失败
                throw new IllegalArgumentException(ics[i] + " is not visible from class loader");

            sb.append(itf).append(';');
        }

        // 使用接口类名列表作为 Key
        String key = sb.toString();

        // 通过类加载器获取缓存
        Map<String, Object> cache;
        synchronized (ProxyCacheMap) {
            cache = ProxyCacheMap.get(cl);
            if (cache == null) {
                cache = new HashMap<String, Object>();
                ProxyCacheMap.put(cl, cache);
            }
        }

        // 获得 TccProxy 工厂
        TccProxy proxy = null;
        synchronized (cache) {
            do {
                // 从缓存中获取 TccProxy 工厂
                Object value = cache.get(key);
                if (value instanceof Reference<?>) {
                    proxy = (TccProxy) ((Reference<?>) value).get();
                    if (proxy != null)
                        return proxy;
                }
                // 缓存中不存在，设置生成 TccProxy 代码标记。创建中时，其他创建请求等待，避免并发。
                if (value == PendingGenerationMarker) {
                    try {
                        cache.wait();
                    } catch (InterruptedException ignored) {
                    }
                } else {
                    cache.put(key, PendingGenerationMarker);
                    break;
                }
            }
            while (true);
        }

        /**
         * 生成 Dubbo Service 调用 Proxy 的代码
         */
        long id = PROXY_CLASS_COUNTER.getAndIncrement();
        String pkg = null;
        //ccp : 生成 Dubbo Service 调用 Proxy 的代码生成器
        //ccm : 生成 Dubbo Service 调用 ProxyFactory 的代码生成器
        TccClassGenerator ccp = null, ccm = null;
        try {
            // 创建 Tcc class 代码生成器
            ccp = TccClassGenerator.newInstance(cl);
            // 已处理方法签名集合。key：方法签名
            Set<String> worked = new HashSet<String>();
            // 已处理方法集合
            List<Method> methods = new ArrayList<Method>();
            // 处理接口
            for (int i = 0; i < ics.length; i++) {
                // 非 public 接口，使用接口包名
                if (!Modifier.isPublic(ics[i].getModifiers())) {
                    String npkg = ics[i].getPackage().getName();
                    if (pkg == null) {
                        pkg = npkg;
                    } else {
                        // 实现了两个非 public 的接口
                        if (!pkg.equals(npkg))
                            throw new IllegalArgumentException("non-public interfaces from different packages");
                    }
                }
                // 添加接口
                ccp.addInterface(ics[i]);

                for (Method method : ics[i].getMethods()) {
                    // 添加方法签名到已处理方法签名集合
                    String desc = ReflectUtils.getDesc(method);
                    if (worked.contains(desc))
                        continue;
                    worked.add(desc);

                    // 生成 Dubbo Service 调用实现代码
                    // 将所有的方法调用都让InvocationHandler统一处理，由IH决定对真实方法的调用
                    int ix = methods.size();
                    Class<?> rt = method.getReturnType();
                    Class<?>[] pts = method.getParameterTypes();
                    StringBuilder code = new StringBuilder("Object[] args = new Object[").append(pts.length).append("];");
                    for (int j = 0; j < pts.length; j++)
                        code.append(" args[").append(j).append("] = ($w)$").append(j + 1).append(";");
                    code.append(" Object ret = handler.invoke(this, methods[").append(ix).append("], args);");
                    if (!Void.TYPE.equals(rt))
                        code.append(" return ").append(asArgument(rt, "ret")).append(";");


                    methods.add(method);
                    StringBuilder compensableDesc = new StringBuilder();
                    // 添加注解
                    Compensable compensable = method.getAnnotation(Compensable.class);

                    //添加生成的方法
                    if (compensable != null) {
                        ccp.addMethod(true, method.getName(), method.getModifiers(), rt, pts, method.getExceptionTypes(), code.toString());
                    } else {
                        ccp.addMethod(false, method.getName(), method.getModifiers(), rt, pts, method.getExceptionTypes(), code.toString());
                    }
                }
            }

            // 设置包路径
            if (pkg == null)
                pkg = PACKAGE_NAME;

            // 设置类名
            String pcn = pkg + ".proxy" + id;
            ccp.setClassName(pcn);
            // 添加静态属性 methods
            ccp.addField("public static java.lang.reflect.Method[] methods;");
            // 添加属性 handler
            ccp.addField("private " + InvocationHandler.class.getName() + " handler;");
            // 添加构造方法，参数 handler
            ccp.addConstructor(Modifier.PUBLIC, new Class<?>[]{InvocationHandler.class}, new Class<?>[0], "handler=$1;");
            // 添加构造方法，参数 空
            ccp.addDefaultConstructor();
            // 生成类
            Class<?> clazz = ccp.toClass();
            // 设置静态属性 methods
            clazz.getField("methods").set(null, methods.toArray(new Method[0]));

            // create TccProxy class.
            String fcn = TccProxy.class.getName() + id;
            // 创建 Tcc class 代码生成器
            ccm = TccClassGenerator.newInstance(cl);
            ccm.setClassName(fcn);
            // 添加构造方法，参数 空
            ccm.addDefaultConstructor();
            // 设置父类为 TccProxy.class
            ccm.setSuperClass(TccProxy.class);
            ccm.addMethod("public Object newInstance(" + InvocationHandler.class.getName() + " h){ return new " + pcn + "($1); }");
            // 生成类
            Class<?> pc = ccm.toClass();
            // 创建 TccProxy 对象
            proxy = (TccProxy) pc.newInstance();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            // release TccClassGenerator
            if (ccp != null)
                ccp.release();
            if (ccm != null)
                ccm.release();
            synchronized (cache) {
                if (proxy == null)
                    cache.remove(key);
                else
                    cache.put(key, new WeakReference<TccProxy>(proxy));
                cache.notifyAll();
            }
        }
        return proxy;
    }


    /**
     * 使用默认 handeler 获取实例。
     *
     * @return instance.
     */
    public Object newInstance() {
        return newInstance(THROW_UNSUPPORTED_INVOKER);
    }

    /**
     * 获取具体 handeler 的实例。
     * TccJavassistProxyFactory 调用该方法，获得 Proxy 。
     *
     * @return instance.
     */
    abstract public Object newInstance(InvocationHandler handler);

    protected TccProxy() {
    }

    private static String asArgument(Class<?> cl, String name) {
        if (cl.isPrimitive()) {
            if (Boolean.TYPE == cl)
                return name + "==null?false:((Boolean)" + name + ").booleanValue()";
            if (Byte.TYPE == cl)
                return name + "==null?(byte)0:((Byte)" + name + ").byteValue()";
            if (Character.TYPE == cl)
                return name + "==null?(char)0:((Character)" + name + ").charValue()";
            if (Double.TYPE == cl)
                return name + "==null?(double)0:((Double)" + name + ").doubleValue()";
            if (Float.TYPE == cl)
                return name + "==null?(float)0:((Float)" + name + ").floatValue()";
            if (Integer.TYPE == cl)
                return name + "==null?(int)0:((Integer)" + name + ").intValue()";
            if (Long.TYPE == cl)
                return name + "==null?(long)0:((Long)" + name + ").longValue()";
            if (Short.TYPE == cl)
                return name + "==null?(short)0:((Short)" + name + ").shortValue()";
            throw new RuntimeException(name + " is unknown primitive type.");
        }
        return "(" + ReflectUtils.getName(cl) + ")" + name;
    }
}