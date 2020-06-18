package io.tyloo.recover;

import java.util.Set;

/*
 * 事务恢复配置接口.
 *
 * @Author:Zh1Cheung zh1cheunglq@gmail.com
 * @Date: 18:50 2019/5/1
 *
 */

public interface TylooRecoverConfiguration {

    /**
     * 获取最大重试次数
     *
     * @return
     */
    int getMaxRetryCount();

    /**
     * 获取需要执行事务恢复的持续时间.
     *
     * @return
     */
    int getRecoverDuration();

    /**
     * 获取定时任务规则表达式.
     *
     * @return
     */
    String getCronExpression();

    /**
     * 延迟取消异常集合
     *
     * @return
     */
    Set<Class<? extends Exception>> getDelayCancelExceptions();

    void setDelayCancelExceptions(Set<Class<? extends Exception>> delayRecoverExceptions);

    int getAsyncTerminateThreadCorePoolSize();

    int getAsyncTerminateThreadMaxPoolSize();

    int getAsyncTerminateThreadWorkQueueSize();
}
