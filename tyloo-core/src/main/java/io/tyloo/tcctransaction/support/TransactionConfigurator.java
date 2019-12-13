package io.tyloo.tcctransaction.support;

import io.tyloo.tcctransaction.TransactionManager;
import io.tyloo.tcctransaction.recover.RecoverConfig;
import io.tyloo.tcctransaction.TransactionRepository;

/*
 *
 * 事务配置器
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 19:34 2019/12/4
 *
 */
public interface TransactionConfigurator {
    /**
     * 获取事务管理器.
     *
     * @return
     */
    public TransactionManager getTransactionManager();

    /**
     * 获取事务库.
     *
     * @return
     */
    public TransactionRepository getTransactionRepository();

    /**
     * 获取事务恢复配置.
     *
     * @return
     */
    public RecoverConfig getRecoverConfig();

}
