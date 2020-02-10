
package io.tyloo.core.disruptor;

import com.lmax.disruptor.RingBuffer;
import io.tyloo.core.disruptor.event.DataEvent;
import io.tyloo.core.disruptor.event.TylooTransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DisruptorProvider.
 * disruptor provider definition.
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 22:00 2020/2/9
 *
 */
public class DisruptorProvider<T> {

    private final RingBuffer<DataEvent<T>> ringBuffer;

    /**
     * The Logger.
     */
    private Logger logger = LoggerFactory.getLogger(DisruptorProvider.class);

    /**
     * Instantiates a new Disruptor provider.
     *
     * @param ringBuffer the ring buffer
     */
    DisruptorProvider(final RingBuffer<DataEvent<T>> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    /**
     * push data to disruptor queue.
     *
     * @param t the t
     */
    public void onData(final TylooTransactionEvent t) {
        long position = ringBuffer.next();
        try {
            DataEvent<T> de = ringBuffer.get(position);
            de.setT((T) t);
            ringBuffer.publish(position);
        } catch (Exception ex) {
            logger.error("push data error:", ex);
        }
    }
}
