

package io.tyloo.tcctransaction.Config;

import io.tyloo.tcctransaction.serializer.KryoPoolSerializer;
import lombok.Data;

/**
 * hmily config.
 *
 * @author xiaoyu
 */
@Data
public class TylooConfig {

    /**
     * Resource suffix this parameter please fill in about is the transaction store path.
     * If it's a table store this is a table suffix, it's stored the same way.
     * If this parameter is not filled in, the applicationName of the application is retrieved by default
     */
    private String repositorySuffix;

    /**
     * this is map db concurrencyScale.
     */
    private Integer concurrencyScale = 512;

    /**
     * log serializer.
     * {@linkplain KryoPoolSerializer}
     */
    private String serializer = "kryo";

    /**
     * scheduledPool Thread size.
     */
    private int scheduledThreadMax = Runtime.getRuntime().availableProcessors() << 1;

    /**
     * scheduledPool scheduledDelay unit SECONDS.
     */
    private int scheduledDelay = 60;

    /**
     * scheduledPool scheduledInitDelay unit SECONDS.
     */
    private int scheduledInitDelay = 120;

    /**
     * retry max.
     */
    private int retryMax = 3;

    /**
     * recoverDelayTime Unit seconds
     * (note that this time represents how many seconds after the local transaction was created before execution).
     */
    private int recoverDelayTime = 60;

    /**
     * Parameters when participants perform their own recovery.
     * 1.such as RPC calls time out
     * 2.such as the starter down machine
     */
    private int loadFactor = 2;

    /**
     * disruptor bufferSize.
     */
    private int bufferSize = 4096 * 2 * 2;

    /**
     * this is disruptor consumerThreads.
     */
    private int consumerThreads = Runtime.getRuntime().availableProcessors() << 1;

    /**
     * this is hmily async execute cancel or confirm thread size.
     */
    private int asyncThreads = Runtime.getRuntime().availableProcessors() << 1;

    /**
     * when start this set true  actor set false.
     */
    private Boolean started = true;


}
