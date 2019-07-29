package io.tyloo.spring;

import io.tyloo.TransactionManager;
import io.tyloo.interceptor.TylooTransactionAspect;
import io.tyloo.interceptor.TylooTransactionInterceptor;
import io.tyloo.support.TransactionConfigurator;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;

/*
 *
 * 可配置的可补偿事务切面
 *
 * @Author:Zh1Cheung zh1cheunglq@gmail.com
 * @Date: 20:13 2019/10/7
 *
 */
@Aspect
public class ConfigurableTransactionAspect extends TylooTransactionAspect implements Ordered {

    private TransactionConfigurator transactionConfigurator;

    /**
     * 初始化
     * 往拦截器中注入 DelayCancelExceptions 和 setTylooTransactionManager
     */
    public void init() {

        TransactionManager transactionManager = transactionConfigurator.getTransactionManager();

        TylooTransactionInterceptor tylooTransactionInterceptor = new TylooTransactionInterceptor();
        tylooTransactionInterceptor.setTransactionManager(transactionManager);
        tylooTransactionInterceptor.setDelayCancelExceptions(transactionConfigurator.getTylooRecoverConfiguration().getDelayCancelExceptions());

        this.setTylooTransactionInterceptor(tylooTransactionInterceptor);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    public void setTransactionConfigurator(TransactionConfigurator transactionConfigurator) {
        this.transactionConfigurator = transactionConfigurator;
    }
}
