package io.tyloo.core.support;

import io.tyloo.api.common.TylooTransactionManager;
import io.tyloo.core.recover.TylooTransactionRecoverConfig;
import io.tyloo.core.repository.TransactionRepository;

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
    public TylooTransactionManager getTylooTransactionManager();

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
    public TylooTransactionRecoverConfig getTylooTransactionRecoverConfig();

}
