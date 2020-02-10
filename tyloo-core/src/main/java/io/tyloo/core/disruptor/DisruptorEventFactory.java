
package io.tyloo.core.disruptor;

import com.lmax.disruptor.EventFactory;
import io.tyloo.core.disruptor.event.DataEvent;

/**
 * DisruptorEventFactory.
 * disruptor Create a factory implementation of the object.
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 22:00 2020/2/9
 *
 */
public class DisruptorEventFactory<T> implements EventFactory<DataEvent<T>> {
    @Override
    public DataEvent<T> newInstance() {
        return new DataEvent<>();
    }
}
