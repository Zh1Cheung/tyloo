

package io.tyloo.core.Config;

import io.tyloo.core.serializer.KryoPoolSerializer;
import lombok.Data;

/*
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 18:48 2020/2/10
 *
 */


@Data
public class TylooConfig {

    /**
     * 序列化日志.
     * {@linkplain KryoPoolSerializer}
     */
    private String serializer = "kryo";

    /**
     * scheduledPool Thread size.
     */
    private int scheduledThreadMax = Runtime.getRuntime().availableProcessors() << 1;

    /**
     * disruptor的bufferSize
     */
    private int bufferSize = 4096 * 2 * 2;

    /**
     * distuptor消费线程数量
     */
    private int consumerThreads = Runtime.getRuntime().availableProcessors() << 1;

    /**
     * 异步执行confirm和cancel线程池线程的大小
     */
    private int asyncThreads = Runtime.getRuntime().availableProcessors() << 1;

    /**
     * 发起方的时候，把此属性设置为true，参与方为false。
     */
    private Boolean started = true;


}
