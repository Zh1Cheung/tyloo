package io.tyloo.core.recover;

import java.util.Set;

/*
 * 事务恢复配置接口.
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 18:50 2019/12/4
 *
 */
public interface RecoverConfig {

    /**
     * 获取最大重试次数
     *
     * @return
     */
    public int getMaxRetryCount();

    /**
     * 获取需要执行事务恢复的持续时间.
     *
     * @return
     */
    public int getRecoverDuration();

    /**
     * 获取定时任务规则表达式.
     *
     * @return
     */
    public String getCronExpression();

    /**
     * 延迟取消异常集合
     *
     * @return
     */
    public Set<Class<? extends Exception>> getDelayCancelExceptions();

    public void setDelayCancelExceptions(Set<Class<? extends Exception>> delayRecoverExceptions);

    public int getAsyncTerminateThreadPoolSize();
}
