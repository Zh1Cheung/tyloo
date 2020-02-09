
package io.tyloo.tcctransaction.disruptor;


/**
 * DisruptorConsumerFactory.
 * Create a subclass implementation object via the {@link #create()} method,
 * which is called in {@link DisruptorConsumer#onEvent(DataEvent)}.
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 22:00 2020/2/9
 */
public interface DisruptorConsumerFactory<T> {

    /**
     * Fix name string.
     *
     * @return the string
     */
    String fixName();

    /**
     * Create disruptor consumer executor.
     *
     * @return the disruptor consumer executor
     */
    AbstractDisruptorConsumerExecutor<T> create();
}
