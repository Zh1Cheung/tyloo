
package io.tyloo.core.disruptor;

import java.util.HashSet;
import java.util.Set;

/**

 * @param <T> the type parameter
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 22:00 2020/2/9
 *
 */
public abstract class AbstractDisruptorConsumerExecutor<T> {

    /**
     * Recorded the subscription processing after the user needs to subscribe to the calculation result.
     */
    private Set<ExecutorSubscriber> subscribers = new HashSet<>();

    /**
     * Add subscribers disruptor consumer executor.
     *
     * @param subscriber subscriber；
     * @return the disruptor consumer executor
     */
    public AbstractDisruptorConsumerExecutor addSubscribers(final ExecutorSubscriber subscriber) {
        subscribers.add(subscriber);
        return this;
    }

    /**
     * Add subscribers disruptor consumer executor.
     *
     * @param subscribers the subscribers
     * @return the disruptor consumer executor
     */
    public AbstractDisruptorConsumerExecutor addSubscribers(final Set<ExecutorSubscriber> subscribers) {
        subscribers.forEach(this::addSubscribers);
        return this;
    }

    /**
     * Gets subscribers.
     *
     * @return the subscribers
     */
    public Set<ExecutorSubscriber> getSubscribers() {
        return subscribers;
    }

    /**
     * Perform the processing of the current event.
     *
     * @param data the data
     */
    public abstract void executor(T data);
}
