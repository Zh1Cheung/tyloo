

package io.tyloo.tcctransaction.concurrent;


import io.tyloo.tcctransaction.concurrent.threadpool.TylooThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/*
 *
 * 单个任务的执行线程
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 21:23 2020/2/9
 *
 */
public class SingletonExecutor extends ThreadPoolExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SingletonExecutor.class);

    private static final int QUEUE_SIZE = 5000;

    private static final RejectedExecutionHandler HANDLER = (r, executor) -> {
        BlockingQueue<Runnable> queue = executor.getQueue();
        while (queue.size() >= QUEUE_SIZE) {
            if (executor.isShutdown()) {
                throw new RejectedExecutionException("SingletonExecutor closed");
            }
            try {
                ((SingletonExecutor) executor).onRejected();
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException ignored) {
            }
        }
        executor.execute(r);
    };

    /**
     * thread name.
     */
    private String name;

    /**
     * Instantiates a new Singleton executor.
     *
     * @param poolName the pool name
     */
    public SingletonExecutor(final String poolName) {
        super(1, 1, 0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(QUEUE_SIZE),
                TylooThreadFactory.create(poolName, false),
                HANDLER);
        this.name = poolName;
    }

    private void onRejected() {
        LOGGER.info("...thread:{}, Saturation occurs, actuator:{}", Thread.currentThread().getName(), name);
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }
}
