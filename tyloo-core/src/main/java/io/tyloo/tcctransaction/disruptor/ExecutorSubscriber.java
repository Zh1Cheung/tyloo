

package io.tyloo.tcctransaction.disruptor;

import java.util.Collection;

/**
 * The interface Executor subscriber.
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 22:00 2020/2/9
 *
 */
public interface ExecutorSubscriber<T> {

    /**
     * Executor.
     *
     * @param collections the collections
     */
    void executor(Collection<? extends T> collections);
}
