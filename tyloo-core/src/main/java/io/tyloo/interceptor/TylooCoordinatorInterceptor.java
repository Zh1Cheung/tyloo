package io.tyloo.interceptor;

import io.tyloo.InvocationContext;
import io.tyloo.Participant;
import io.tyloo.Transaction;
import io.tyloo.TransactionManager;
import io.tyloo.api.Tyloo;
import io.tyloo.api.TransactionContext;
import io.tyloo.api.TransactionStatus;
import io.tyloo.api.TransactionXid;
import io.tyloo.support.FactoryBuilder;
import io.tyloo.utils.ReflectionUtils;
import io.tyloo.utils.TylooMethodUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

/*
 * 资源协调拦截器
 *
 * @Author:Zh1Cheung zh1cheunglq@gmail.com
 * @Date: 15:35 2019/4/23
 *
 */


public class TylooCoordinatorInterceptor {

    private TransactionManager transactionManager;


    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public Object interceptTransactionContextMethod(ProceedingJoinPoint pjp) throws Throwable {

        Transaction transaction = transactionManager.getCurrentTransaction();

        if (transaction != null) {

            switch (transaction.getStatus()) {
                case TRYING:
                    enlistParticipant(pjp);
                    break;
                case CONFIRMING:
                    break;
                case CANCELLING:
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + transaction.getStatus());
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

        Method method = TylooMethodUtils.getTylooMethod(pjp);
        if (method == null) {
            throw new RuntimeException(String.format("join point not found method, point is : %s", pjp.getSignature().getName()));
        }
        Tyloo tyloo = method.getAnnotation(Tyloo.class);

        String confirmMethodName = tyloo.confirmMethod();
        String cancelMethodName = tyloo.cancelMethod();

        Transaction transaction = transactionManager.getCurrentTransaction();
        TransactionXid xid = new TransactionXid(transaction.getXid().getGlobalTransactionId());

        if (FactoryBuilder.factoryOf(tyloo.transactionContextEditor()).getInstance().get(pjp.getTarget(), method, pjp.getArgs()) == null) {
            FactoryBuilder.factoryOf(tyloo.transactionContextEditor()).getInstance().set(new TransactionContext(xid, TransactionStatus.TRYING.getId()), pjp.getTarget(), ((MethodSignature) pjp.getSignature()).getMethod(), pjp.getArgs());
        }

        Class targetClass = ReflectionUtils.getDeclaringType(pjp.getTarget().getClass(), method.getName(), method.getParameterTypes());

        InvocationContext confirmInvocation = new InvocationContext(targetClass,
                confirmMethodName,
                method.getParameterTypes(), pjp.getArgs());

        InvocationContext cancelInvocation = new InvocationContext(targetClass,
                cancelMethodName,
                method.getParameterTypes(), pjp.getArgs());

        Participant participant =
                new Participant(
                        xid,
                        confirmInvocation,
                        cancelInvocation,
                        tyloo.transactionContextEditor());

        transactionManager.enlistParticipant(participant);

    }


}
