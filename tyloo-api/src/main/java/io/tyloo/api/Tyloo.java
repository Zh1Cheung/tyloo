package io.tyloo.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;



/*
 *
 * 事务补偿注解
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 11:32 2019/12/4
 *
 *
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Tyloo {

    /**
     * 传播级别
     */
    public Propagation propagation() default Propagation.REQUIRED;

    /**
     * 确认执行业务方法
     */
    public String confirmMethod() default "";

    /**
     * 取消执行业务方法
     */
    public String cancelMethod() default "";

    /**
     * 事务上下文编辑
     */
    public Class<? extends TylooContextLoader> tylooContextLoader() default DefaultTylooContextLoader.class;

    /**
     * 超时异常
     * delayCancelExceptions()表示系统发生了设置的异常时，主事务不立即rollbac，而是由恢复job来执行事务恢复。k
     * 通常需要将超时异常设置为delayCancelExceptions，这样可以避免因为服务调用时发生了超时异常，主事务如果立刻rollback, 但是从事务还没执行完，从而造成主事务rollback失败
     *
     * @return
     */
    public Class<? extends Exception>[] delayCancelExceptions() default {};

    public boolean asyncConfirm() default false;

    public boolean asyncCancel() default false;

    //无事务上下文编辑器实现
    class NullableTylooContextLoader implements TylooContextLoader {

        @Override
        public TylooContext get(Object target, Method method, Object[] args) {
            return null;
        }

        @Override
        public void set(TylooContext tylooContext, Object target, Method method, Object[] args) {

        }
    }

    //默认事务上下文编辑器实现
    class DefaultTylooContextLoader implements TylooContextLoader {

        @Override
        public TylooContext get(Object target, Method method, Object[] args) {
            int position = getTransactionContextParamPosition(method.getParameterTypes());

            if (position >= 0) {
                return (TylooContext) args[position];
            }

            return null;
        }

        @Override
        public void set(TylooContext tylooContext, Object target, Method method, Object[] args) {

            int position = getTransactionContextParamPosition(method.getParameterTypes());
            if (position >= 0) {
                args[position] = tylooContext;
            }
        }

        /**
         * 获得事务上下文在方法参数里的位置
         *
         * @param parameterTypes 参数类型集合
         * @return 位置
         */
        public static int getTransactionContextParamPosition(Class<?>[] parameterTypes) {

            int position = -1;

            for (int i = 0; i < parameterTypes.length; i++) {
                if (parameterTypes[i].equals(TylooContext.class)) {
                    position = i;
                    break;
                }
            }
            return position;
        }

        /**
         * @param args 参数列表
         * @return 获取TransactionContext对象
         */
        public static TylooContext getTransactionContextFromArgs(Object[] args) {

            TylooContext tylooContext = null;

            for (Object arg : args) {
                if (arg != null && TylooContext.class.isAssignableFrom(arg.getClass())) {

                    tylooContext = (TylooContext) arg;
                }
            }

            return tylooContext;
        }

    }
}