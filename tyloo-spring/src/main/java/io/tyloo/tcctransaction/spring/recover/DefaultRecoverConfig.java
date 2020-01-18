package io.tyloo.tcctransaction.spring.recover;

import io.tyloo.tcctransaction.exception.OptimisticLockException;
import io.tyloo.tcctransaction.recover.RecoverConfig;

import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.Set;

/*
 *
 * 默认事务恢复配置
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 20:14 2019/12/4
 *
 */
public class DefaultRecoverConfig implements RecoverConfig {

    public static final RecoverConfig INSTANCE = new DefaultRecoverConfig();

    /**
     * 一个事务最多尝试恢复次数（超过将不在自动恢复，需要人工干预，默认是30次）
     */
    private int maxRetryCount = 30;

    /**
     * 一个事务日志当超过一定时间间隔后没有更新就会被认为是发生了异常，需要恢复，
     * 恢复Job将扫描超过这个时间间隔依旧没有更新的事务日志，并对这些事务进行恢复，时间单位是秒，默认是120秒
     */
    private int recoverDuration = 120; //120 seconds

    /**
     * 恢复Job触发间隔配置，默认是(每分钟)
     * cron 表达式
     * 0/30 * * * * ?，每 30 秒执行一次。
     */
    private String cronExpression = "0 */1 * * * ?";

    private int asyncTerminateThreadPoolSize = 1024;

    /**
     * 延迟取消异常集合
     */
    private Set<Class<? extends Exception>> delayCancelExceptions = new HashSet<Class<? extends Exception>>();

    public DefaultRecoverConfig() {

        /**
         * 还是 SocketTimeoutException 的情况，事务恢复间隔时间小于 Socket 超时时间，此时事务恢复调用远程参与者取消回滚事务，
         * 远程参与者下次更新事务时，会因为乐观锁更新失败，抛出 OptimisticLockException。如果 TylooInterceptor 此时立刻取消回滚，
         * 可能会和定时任务的取消回滚冲突，因此统一交给定时任务处理。
         *
         */
        delayCancelExceptions.add(OptimisticLockException.class);
        /**
         * try 阶段，本地参与者调用远程参与者( 远程服务，例如 Dubbo，Http 服务)，远程参与者 try 阶段的方法逻辑执行时间较长，超过 Socket 等待时长，发生 SocketTimeoutException，
         * 如果立刻执行事务回滚，远程参与者 try 的方法未执行完成，可能导致 cancel 的方法实际未执行( try 的方法未执行完成，数据库事务【非 TCC 事务】未提交，
         * cancel 的方法读取数据时发现未变更，导致方法实际未执行，最终 try 的方法执行完后，提交数据库事务【非 TCC 事务】，较为极端 )，最终引起数据不一致。
         * 在事务恢复时，会对这种情况的事务进行取消回滚，如果此时远程参与者的 try 的方法还未结束，还是可能发生数据不一致。
         *
         */
        delayCancelExceptions.add(SocketTimeoutException.class);
    }

    @Override
    public int getMaxRetryCount() {
        return maxRetryCount;
    }

    @Override
    public int getRecoverDuration() {
        return recoverDuration;
    }

    @Override
    public String getCronExpression() {
        return cronExpression;
    }


    public void setMaxRetryCount(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public void setRecoverDuration(int recoverDuration) {
        this.recoverDuration = recoverDuration;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    @Override
    public void setDelayCancelExceptions(Set<Class<? extends Exception>> delayCancelExceptions) {
        this.delayCancelExceptions.addAll(delayCancelExceptions);
    }

    @Override
    public Set<Class<? extends Exception>> getDelayCancelExceptions() {
        return this.delayCancelExceptions;
    }

    public int getAsyncTerminateThreadPoolSize() {
        return asyncTerminateThreadPoolSize;
    }

    public void setAsyncTerminateThreadPoolSize(int asyncTerminateThreadPoolSize) {
        this.asyncTerminateThreadPoolSize = asyncTerminateThreadPoolSize;
    }
}
