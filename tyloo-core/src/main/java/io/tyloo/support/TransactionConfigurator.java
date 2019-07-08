package io.tyloo.support;

import io.tyloo.TransactionManager;
import io.tyloo.TransactionRepository;
import io.tyloo.recover.TylooRecoverConfiguration;

/*
 *
 * 事务配置器
 *
 * @Author:Zh1Cheung zh1cheunglq@gmail.com
 * @Date: 19:34 2019/5/28
 *
 */

public interface TransactionConfigurator {

    /**
     * 获取事务管理器.
     *
     * @return
     */
    TransactionManager getTransactionManager();

    /**
     * 获取事务库.
     *
     * @return
     */
    TransactionRepository getTransactionRepository();

    /**
     * 获取事务恢复配置.
     *
     * @return
     */
    TylooRecoverConfiguration getTylooRecoverConfiguration();
}
