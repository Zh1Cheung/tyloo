
package io.tyloo.core.disruptor;

import com.lmax.disruptor.WorkHandler;
import io.tyloo.core.disruptor.event.DataEvent;

/**
 * DisruptorConsumer.
 * disruptor consumer work handler.
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 22:00 2020/2/9
 *
 */
public class DisruptorConsumer<T> implements WorkHandler<DataEvent<T>> {

    private DisruptorConsumerFactory<T> factory;

    DisruptorConsumer(final DisruptorConsumerFactory<T> factory) {
        this.factory = factory;
    }

    @Override
    public void onEvent(final DataEvent<T> t) {
        if (t != null) {
            factory.create().executor(t.getT());
        }
    }
}
