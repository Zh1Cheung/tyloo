package io.tyloo.tcctransaction.utils;

import io.tyloo.api.Propagation;
import io.tyloo.tcctransaction.interceptor.TylooMethodContext;

/*
 *
 * 事务工具类
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 19:50 2019/12/4
 *
 */
public class TransactionUtils {


    /**

     * 判断事务上下文是否合法
     * 在 Propagation.MANDATORY 必须有在事务内
     *
     * @param isTransactionActive 是否事务开启
     * @param tylooMethodContext 事务上下文
     * @return 是否合法
     */
    public static boolean isLegalTransactionContext(boolean isTransactionActive, TylooMethodContext tylooMethodContext) {


        return !tylooMethodContext.getPropagation().equals(Propagation.MANDATORY) || isTransactionActive || tylooMethodContext.getTylooContext() != null;

    }
}
