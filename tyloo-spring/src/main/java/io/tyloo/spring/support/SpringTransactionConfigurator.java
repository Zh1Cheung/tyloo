package io.tyloo.spring.support;

import io.tyloo.api.common.TylooTransactionManager;
import io.tyloo.core.recover.TylooTransactionRecoverConfig;
import io.tyloo.core.repository.TransactionRepository;
import io.tyloo.core.repository.CachableTransactionRepository;
import io.tyloo.spring.recover.DefaultTylooTransactionRecoverConfig;
import io.tyloo.core.support.TransactionConfigurator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 20:20 2019/12/4
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpringTransactionConfigurator implements TransactionConfigurator {

    private static volatile ExecutorService executorService = null;

    /**
     * 事务库
     */
    @Autowired
    private TransactionRepository transactionRepository;
    /**
     * 事务恢复配置
     */
    @Autowired(required = false)
    private TylooTransactionRecoverConfig tylooTransactionRecoverConfig = DefaultTylooTransactionRecoverConfig.INSTANCE;

    /**
     * 根据事务配置器创建事务管理器.
     */
    private TylooTransactionManager tylooTransactionManager;

    public void init() {
        tylooTransactionManager = new TylooTransactionManager();
        tylooTransactionManager.setTransactionRepository(transactionRepository);

        if (executorService == null) {

            synchronized (SpringTransactionConfigurator.class) {

                if (executorService == null) {
//                    executorService = new ThreadPoolExecutor(tylooTransactionRecoverConfig.getAsyncTerminateThreadPoolSize(),
//                            tylooTransactionRecoverConfig.getAsyncTerminateThreadPoolSize(),
//                            0L, TimeUnit.SECONDS,
//                            new SynchronousQueue<Runnable>());
                    executorService = Executors.newCachedThreadPool();
                }
            }
        }

        tylooTransactionManager.setExecutorService(executorService);

        if (transactionRepository instanceof CachableTransactionRepository) {
            ((CachableTransactionRepository) transactionRepository).setExpireDuration(tylooTransactionRecoverConfig.getRecoverDuration());
        }
    }

    /**
     * 获取事务管理器.
     */
    @Override
    public TylooTransactionManager getTylooTransactionManager() {
        return tylooTransactionManager;
    }

    /**
     * 获取事务库.
     */
    @Override
    public TransactionRepository getTransactionRepository() {
        return transactionRepository;
    }

    /**
     * 获取事务恢复配置.
     */
    @Override
    public TylooTransactionRecoverConfig getTylooTransactionRecoverConfig() {
        return tylooTransactionRecoverConfig;
    }
}
