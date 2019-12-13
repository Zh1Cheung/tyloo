package io.tyloo.tcctransaction.spring;

import org.aspectj.lang.annotation.Aspect;
import io.tyloo.tcctransaction.TransactionManager;
import io.tyloo.tcctransaction.interceptor.CompensableTransactionAspect;
import io.tyloo.tcctransaction.interceptor.CompensableTransactionInterceptor;
import io.tyloo.tcctransaction.support.TransactionConfigurator;
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
public class ConfigurableTransactionAspect extends CompensableTransactionAspect implements Ordered {

    private TransactionConfigurator transactionConfigurator;

    public void init() {

        TransactionManager transactionManager = transactionConfigurator.getTransactionManager();

        CompensableTransactionInterceptor compensableTransactionInterceptor = new CompensableTransactionInterceptor();
        compensableTransactionInterceptor.setTransactionManager(transactionManager);
        compensableTransactionInterceptor.setDelayCancelExceptions(transactionConfigurator.getRecoverConfig().getDelayCancelExceptions());

        this.setCompensableTransactionInterceptor(compensableTransactionInterceptor);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    public void setTransactionConfigurator(TransactionConfigurator transactionConfigurator) {
        this.transactionConfigurator = transactionConfigurator;
    }
}
