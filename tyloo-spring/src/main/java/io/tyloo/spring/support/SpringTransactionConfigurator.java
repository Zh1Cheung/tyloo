package io.tyloo.spring.support;

import io.tyloo.TransactionManager;
import io.tyloo.TransactionRepository;
import io.tyloo.recover.TylooRecoverConfiguration;
import io.tyloo.repository.CachableTransactionRepository;
import io.tyloo.spring.recover.DefaultTylooRecoverConfiguration;
import io.tyloo.support.TransactionConfigurator;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/*
 *
 * Spring ¬ŒÒ≈‰÷√¿‡
 *
 * @Author:Zh1Cheung zh1cheunglq@gmail.com
 * @Date: 20:20 2019/12/4
 *
 */
public class SpringTransactionConfigurator implements TransactionConfigurator {

    private static volatile ExecutorService executorService = null;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired(required = false)
    private TylooRecoverConfiguration tylooRecoverConfiguration = DefaultTylooRecoverConfiguration.INSTANCE;


    private TransactionManager transactionManager;

    public void init() {
        transactionManager = new TransactionManager();
        transactionManager.setTransactionRepository(transactionRepository);

        if (executorService == null) {


            Executors.defaultThreadFactory();
            synchronized (SpringTransactionConfigurator.class) {

                if (executorService == null) {
                    executorService = new ThreadPoolExecutor(
                            tylooRecoverConfiguration.getAsyncTerminateThreadCorePoolSize(),
                            tylooRecoverConfiguration.getAsyncTerminateThreadMaxPoolSize(),
                            5L,
                            TimeUnit.SECONDS,
                            new ArrayBlockingQueue<Runnable>(tylooRecoverConfiguration.getAsyncTerminateThreadWorkQueueSize()),
                            new ThreadFactory() {

                                final AtomicInteger poolNumber = new AtomicInteger(1);
                                final ThreadGroup group;
                                final AtomicInteger threadNumber = new AtomicInteger(1);
                                final String namePrefix;

                                {
                                    SecurityManager securityManager = System.getSecurityManager();
                                    this.group = securityManager != null ? securityManager.getThreadGroup() : Thread.currentThread().getThreadGroup();
                                    this.namePrefix = "tcc-async-terminate-pool-" + poolNumber.getAndIncrement() + "-thread-";
                                }

                                @Override
                                public Thread newThread(Runnable runnable) {
                                    Thread thread = new Thread(this.group, runnable, this.namePrefix + this.threadNumber.getAndIncrement(), 0L);
                                    if (thread.isDaemon()) {
                                        thread.setDaemon(false);
                                    }

                                    if (thread.getPriority() != 5) {
                                        thread.setPriority(5);
                                    }

                                    return thread;
                                }
                            },
                            new ThreadPoolExecutor.CallerRunsPolicy());
                }
            }
        }

        transactionManager.setExecutorService(executorService);

        if (transactionRepository instanceof CachableTransactionRepository) {
            ((CachableTransactionRepository) transactionRepository).setExpireDuration(tylooRecoverConfiguration.getRecoverDuration());
        }
    }

    @Override
    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    @Override
    public TransactionRepository getTransactionRepository() {
        return transactionRepository;
    }

    @Override
    public TylooRecoverConfiguration getTylooRecoverConfiguration() {
        return tylooRecoverConfiguration;
    }
}
