package io.tyloo.tcctransaction.interceptor;

import io.tyloo.tcctransaction.InvocationContext;
import io.tyloo.tcctransaction.Participant;
import io.tyloo.tcctransaction.Transaction;
import io.tyloo.tcctransaction.TransactionManager;
import io.tyloo.tcctransaction.support.FactoryBuilder;
import io.tyloo.tcctransaction.utils.CompensableMethodUtils;
import io.tyloo.tcctransaction.utils.ReflectionUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import io.tyloo.api.Compensable;
import io.tyloo.api.TransactionContext;
import io.tyloo.api.TransactionStatus;
import io.tyloo.api.TransactionXid;

import java.lang.reflect.Method;

/*
 * 资源协调拦截器
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 15:35 2019/12/4
 *
 */
public class ResourceCoordinatorInterceptor {
    /**
     * 事务管理器.
     */
    private TransactionManager transactionManager;

    /**
     * 设置事务管理器.
     *
     * @param transactionManager
     */
    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    /**
     * 拦截事务上下文方法.
     *
     * @param pjp
     * @throws Throwable
     */
    public Object interceptTransactionContextMethod(ProceedingJoinPoint pjp) throws Throwable {
        // 获取当前事务
        Transaction transaction = transactionManager.getCurrentTransaction();
        // Trying(判断是否Try阶段的事务)
        if (transaction != null) {

            switch (transaction.getStatus()) {
                case TRYING:
                    enlistParticipant(pjp);
                    break;
                case CONFIRMING:
                    break;
                case CANCELLING:
                    break;
            }
        }

        return pjp.proceed(pjp.getArgs());
    }

    /**
     * 添加事务参与者，在事务处于 Try 阶段被调用
     *
     * @param pjp
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private void enlistParticipant(ProceedingJoinPoint pjp) throws IllegalAccessException, InstantiationException {

        // 获得 @Compensable 注解
        Method method = CompensableMethodUtils.getCompensableMethod(pjp);
        if (method == null) {
            throw new RuntimeException(String.format("join point not found method, point is : %s", pjp.getSignature().getName()));
        }
        Compensable compensable = method.getAnnotation(Compensable.class);

        // 获得 确认执行业务方法 和 取消执行业务方法
        String confirmMethodName = compensable.confirmMethod();
        String cancelMethodName = compensable.cancelMethod();

        // 获取 当前线程事务第一个(头部)元素
        Transaction transaction = transactionManager.getCurrentTransaction();
        // 创建 事务编号
        TransactionXid xid = new TransactionXid(transaction.getXid().getGlobalTransactionId());
        //如果该注解的单例类的参数中没有事务上下文 便新建一个事务上下文
        if (FactoryBuilder.factoryOf(compensable.transactionContextEditor()).getInstance().get(pjp.getTarget(), method, pjp.getArgs()) == null) {
            FactoryBuilder.factoryOf(compensable.transactionContextEditor()).getInstance().set(new TransactionContext(xid, TransactionStatus.TRYING.getId()), pjp.getTarget(), ((MethodSignature) pjp.getSignature()).getMethod(), pjp.getArgs());
    }

        // 获得类
        Class targetClass = ReflectionUtils.getDeclaringType(pjp.getTarget().getClass(), method.getName(), method.getParameterTypes());
        // 构建确认方法的提交上下文
        InvocationContext confirmInvocation = new InvocationContext(targetClass,
                confirmMethodName,
                method.getParameterTypes(), pjp.getArgs());
        // 构建取消方法的提交上下文
        InvocationContext cancelInvocation = new InvocationContext(targetClass,
                cancelMethodName,
                method.getParameterTypes(), pjp.getArgs());
        // 构建参与者对象
        Participant participant =
                new Participant(
                        xid,
                        confirmInvocation,
                        cancelInvocation,
                        compensable.transactionContextEditor());

        transactionManager.enlistParticipant(participant);

    }


}
