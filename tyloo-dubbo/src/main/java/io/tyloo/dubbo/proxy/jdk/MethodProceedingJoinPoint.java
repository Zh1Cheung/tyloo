package io.tyloo.dubbo.proxy.jdk;

import io.tyloo.SystemException;
import io.tyloo.utils.ReflectionUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.reflect.SourceLocation;
import org.aspectj.runtime.internal.AroundClosure;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


/*
 *
 * 生成方法切面
 *
 * 该类参考 [`org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint`](https://github.com/spring-projects/spring-framework/blob/master/spring-aop/src/main/java/org/springframework/aop/aspectj/MethodInvocationProceedingJoinPoint.java) 实现。
 * 在切面处理完成后，调用 `#proceed(...)` 方法，进行远程 Dubbo Service 服务调用。
 *
 * @Author:Zh1Cheung zh1cheunglq@gmail.com
 * @Date: 10:56 2019/9/12
 *
 */

public class MethodProceedingJoinPoint implements ProceedingJoinPoint, JoinPoint.StaticPart {

    private final Object proxy;

    private final Object target;

    private final Method method;

    private final Object[] args;

    private Signature signature;

    /**
     * Lazily initialized source location object
     */
    private SourceLocation sourceLocation;

    /**
     * @param proxy  代理对象
     * @param target 目标对象
     * @param method 方法
     * @param args   参数
     */
    public MethodProceedingJoinPoint(Object proxy, Object target, Method method, Object[] args) {
        this.proxy = proxy;
        this.target = target;
        this.method = method;
        this.args = args;
    }

    /**
     * 远程 Dubbo Service 服务调用
     *
     * @return
     * @throws Throwable
     */
    @Override
    public void set$AroundClosure(AroundClosure aroundClosure) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object proceed() throws Throwable {

        // Use reflection to invoke the method.
        try {
            ReflectionUtils.makeAccessible(method);
            return method.invoke(target, args);
        } catch (InvocationTargetException ex) {
            // Invoked method threw a checked exception.
            // We must rethrow it. The client won't see the interceptor.
            throw ex.getTargetException();
        } catch (IllegalArgumentException ex) {
            throw new SystemException("Tried calling method [" +
                    method + "] on target [" + target + "] failed", ex);
        } catch (IllegalAccessException ex) {
            throw new SystemException("Could not access method [" + method + "]", ex);
        }
    }

    @Override
    public Object proceed(Object[] objects) throws Throwable {

        // Use reflection to invoke the method.
        try {
            ReflectionUtils.makeAccessible(method);
            return method.invoke(target, objects);
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        } catch (IllegalArgumentException ex) {
            throw new SystemException("Tried calling method [" +
                    method + "] on target [" + target + "] failed", ex);
        } catch (IllegalAccessException ex) {
            throw new SystemException("Could not access method [" + method + "]", ex);
        }
    }

    @Override
    public String toShortString() {
        return "execution(" + getSignature().toShortString() + ")";
    }

    @Override
    public String toLongString() {
        return "execution(" + getSignature().toLongString() + ")";
    }

    @Override
    public String toString() {
        return "execution(" + getSignature().toString() + ")";
    }

    @Override
    public Object getThis() {
        return this.proxy;
    }

    @Override
    public Object getTarget() {
        return this.target;
    }

    @Override
    public Object[] getArgs() {
        return this.args;
    }

    @Override
    public Signature getSignature() {
        if (this.signature == null) {
            this.signature = new MethodSignatureImpl();
        }
        return signature;
    }

    @Override
    public SourceLocation getSourceLocation() {
        if (this.sourceLocation == null) {
            this.sourceLocation = new SourceLocationImpl();
        }
        return this.sourceLocation;
    }

    @Override
    public String getKind() {
        return ProceedingJoinPoint.METHOD_EXECUTION;
    }

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public StaticPart getStaticPart() {
        return this;
    }

    /**
     * Lazily initialized MethodSignature.
     */
    private class MethodSignatureImpl implements MethodSignature {

        private volatile String[] parameterNames;

        @Override
        public String getName() {
            return method.getName();
        }

        @Override
        public int getModifiers() {
            return method.getModifiers();
        }

        @Override
        public Class getDeclaringType() {
            return method.getDeclaringClass();
        }

        @Override
        public String getDeclaringTypeName() {
            return method.getDeclaringClass().getName();
        }

        @Override
        public Class getReturnType() {
            return method.getReturnType();
        }

        @Override
        public Method getMethod() {
            return method;
        }

        @Override
        public Class[] getParameterTypes() {
            return method.getParameterTypes();
        }

        @Override
        public String[] getParameterNames() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Class[] getExceptionTypes() {
            return method.getExceptionTypes();
        }

        @Override
        public String toShortString() {
            return toString(false, false, false, false);
        }

        @Override
        public String toLongString() {
            return toString(true, true, true, true);
        }

        @Override
        public String toString() {
            return toString(false, true, false, true);
        }

        private String toString(boolean includeModifier, boolean includeReturnTypeAndArgs,
                                boolean useLongReturnAndArgumentTypeName, boolean useLongTypeName) {
            StringBuilder sb = new StringBuilder();
            if (includeModifier) {
                sb.append(Modifier.toString(getModifiers()));
                sb.append(" ");
            }
            if (includeReturnTypeAndArgs) {
                appendType(sb, getReturnType(), useLongReturnAndArgumentTypeName);
                sb.append(" ");
            }
            appendType(sb, getDeclaringType(), useLongTypeName);
            sb.append(".");
            sb.append(getMethod().getName());
            sb.append("(");
            Class[] parametersTypes = getParameterTypes();
            appendTypes(sb, parametersTypes, includeReturnTypeAndArgs, useLongReturnAndArgumentTypeName);
            sb.append(")");
            return sb.toString();
        }

        private void appendTypes(StringBuilder sb, Class<?>[] types,
                                 boolean includeArgs, boolean useLongReturnAndArgumentTypeName) {
            if (includeArgs) {
                for (int size = types.length, i = 0; i < size; i++) {
                    appendType(sb, types[i], useLongReturnAndArgumentTypeName);
                    if (i < size - 1) {
                        sb.append(",");
                    }
                }
            } else {
                if (types.length != 0) {
                    sb.append("..");
                }
            }
        }

        private void appendType(StringBuilder sb, Class<?> type, boolean useLongTypeName) {
            if (type.isArray()) {
                appendType(sb, type.getComponentType(), useLongTypeName);
                sb.append("[]");
            } else {
                sb.append(useLongTypeName ? type.getName() : type.getSimpleName());
            }
        }
    }


    /**
     * Lazily initialized SourceLocation.
     */
    private class SourceLocationImpl implements SourceLocation {

        @Override
        public Class getWithinType() {
            if (proxy == null) {
                throw new UnsupportedOperationException("No source location joinpoint available: target is null");
            }
            return proxy.getClass();
        }

        @Override
        public String getFileName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getLine() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getColumn() {
            throw new UnsupportedOperationException();
        }
    }

}
