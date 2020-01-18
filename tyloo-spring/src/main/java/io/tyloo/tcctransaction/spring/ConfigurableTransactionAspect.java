package io.tyloo.tcctransaction.spring;

import io.tyloo.tcctransaction.TransactionManager;
import io.tyloo.tcctransaction.interceptor.TylooAspect;
import io.tyloo.tcctransaction.interceptor.TylooInterceptor;
import io.tyloo.tcctransaction.support.TransactionConfigurator;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;

/*
 *
 * 可配置的可补偿事务切面
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 20:13 2019/12/4
 *
 */
@Aspect
public class ConfigurableTransactionAspect extends TylooAspect implements Ordered {

    private TransactionConfigurator transactionConfigurator;

    public void init() {

        TransactionManager transactionManager = transactionConfigurator.getTransactionManager();

        TylooInterceptor TylooTransactionInterceptor = new TylooInterceptor();
        TylooTransactionInterceptor.setTransactionManager(transactionManager);
        TylooTransactionInterceptor.setDelayCancelExceptions(transactionConfigurator.getRecoverConfig().getDelayCancelExceptions());

        this.setTylooInterceptor(TylooTransactionInterceptor);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    public void setTransactionConfigurator(TransactionConfigurator transactionConfigurator) {
        this.transactionConfigurator = transactionConfigurator;
    }
}
