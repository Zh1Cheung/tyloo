package io.tyloo.api.Context;

import java.lang.reflect.Method;


/*
 *
 * 事务上下文编辑器，用于设置和获得事务上下文( TylooTransactionContext )
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 11:35 2019/12/4
 *
 */

public interface TylooTransactionContextLoader {

    /**
     * 从参数中获得事务上下文
     *
     * @param target 对象
     * @param method 方法
     * @param args   参数
     * @return 事务上下文
     */

    public TylooTransactionContext get(Object target, Method method, Object[] args);

    /**
     * 设置事务上下文到参数中
     *
     * @param tylooTransactionContext 事务上下文
     * @param target             对象
     * @param method             方法
     * @param args               参数
     */
    public void set(TylooTransactionContext tylooTransactionContext, Object target, Method method, Object[] args);

}
