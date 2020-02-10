package io.tyloo.core.interceptor;

import io.tyloo.api.Annotation.Tyloo;
import io.tyloo.api.Context.TylooContext;
import io.tyloo.api.Context.TylooContextLoader;
import io.tyloo.api.Enums.Status;
import io.tyloo.api.Context.InvocationContext;
import io.tyloo.api.common.Participant;
import io.tyloo.api.common.TylooTransaction;
import io.tyloo.api.common.TylooTransactionManager;
import io.tyloo.api.common.TylooTransactionXid;
import io.tyloo.core.support.FactoryBuilder;
import io.tyloo.core.utils.TylooMethodUtils;
import io.tyloo.core.utils.ReflectionUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

/*
 * 资源协调拦截器
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 15:35 2019/12/4
 *
 */
public class TylooCoordinatorInterceptor {
    /**
     * 事务管理器.
     */
    private TylooTransactionManager tylooTransactionManager;

    /**
     * 设置事务管理器.
     *
     * @param tylooTransactionManager
     */
    public void setTylooTransactionManager(TylooTransactionManager tylooTransactionManager) {
        this.tylooTransactionManager = tylooTransactionManager;
    }

    /**
     * 拦截事务上下文方法.
     *
     * @param pjp
     * @throws Throwable
     */
    public Object interceptTransactionContextMethod(ProceedingJoinPoint pjp) throws Throwable {
        // 获取当前事务
        TylooTransaction tylooTransaction = tylooTransactionManager.getCurrentTransaction();
        // Trying(判断是否Try阶段的事务)
        if (tylooTransaction != null) {

            switch (tylooTransaction.getStatus()) {
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

        // 获得 @Tyloo 注解
        Method method = TylooMethodUtils.getTylooMethod(pjp);
        if (method == null) {
            throw new RuntimeException(String.format("join point not found method, point is : %s", pjp.getSignature().getName()));
        }
        Tyloo tyloo = method.getAnnotation(Tyloo.class);

        // 获得 确认执行业务方法 和 取消执行业务方法
        String confirmMethodName = tyloo.confirmMethod();
        String cancelMethodName = tyloo.cancelMethod();

        // 获取 当前线程事务第一个(头部)元素
        TylooTransaction tylooTransaction = tylooTransactionManager.getCurrentTransaction();
        // 创建 事务编号
        TylooTransactionXid xid = new TylooTransactionXid(tylooTransaction.getXid().getGlobalTransactionId());
        //如果该注解的单例类的参数中没有事务上下文 便新建一个事务上下文
        TylooContextLoader instance = (TylooContextLoader) FactoryBuilder.factoryOf(tyloo.tylooContextLoader()).getInstance();
        if (instance.get(pjp.getTarget(), method, pjp.getArgs()) == null) {
            instance.set(new TylooContext(xid, Status.TRYING.getId()), pjp.getTarget(), ((MethodSignature) pjp.getSignature()).getMethod(), pjp.getArgs());
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
                        tyloo.tylooContextLoader());

        tylooTransactionManager.enlistParticipant(participant);

    }


}
