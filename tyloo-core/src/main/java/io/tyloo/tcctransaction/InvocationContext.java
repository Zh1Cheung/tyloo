package io.tyloo.tcctransaction;

import java.io.Serializable;

/*
 *
 *  执行方法调用上下文
 *  记录类、方法名、参数类型数组、参数数组。
 *  通过这些属性，可以执行提交 / 回滚事务。
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 15:48 2019/12/4
 *
 */
public class InvocationContext implements Serializable {

    private static final long serialVersionUID = -7969140711432461165L;
    /**
     * 类
     */
    private Class targetClass;
    /**
     * 方法名
     */
    private String methodName;
    /**
     * 参数类型数组
     */
    private Class[] parameterTypes;
    /**
     * 参数数组
     */
    private Object[] args;

    public InvocationContext() {

    }

    public InvocationContext(Class targetClass, String methodName, Class[] parameterTypes, Object... args) {
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.targetClass = targetClass;
        this.args = args;
    }

    public Object[] getArgs() {
        return args;
    }

    public Class getTargetClass() {
        return targetClass;
    }

    public String getMethodName() {
        return methodName;
    }

    public Class[] getParameterTypes() {
        return parameterTypes;
    }
}
