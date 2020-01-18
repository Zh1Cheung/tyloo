package io.tyloo.tcctransaction.spring;

import io.tyloo.tcctransaction.interceptor.TylooCoordinatorAspect;
import io.tyloo.tcctransaction.interceptor.TylooCoordinatorInterceptor;
import io.tyloo.tcctransaction.support.TransactionConfigurator;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;

/*
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 20:21 2019/12/4
 *
 */
@Aspect
public class ConfigurableCoordinatorAspect extends TylooCoordinatorAspect implements Ordered {

    private TransactionConfigurator transactionConfigurator;

    public void init() {

        TylooCoordinatorInterceptor TylooCoordinatorInterceptor = new TylooCoordinatorInterceptor();
        TylooCoordinatorInterceptor.setTransactionManager(transactionConfigurator.getTransactionManager());
        this.setTylooCoordinatorInterceptor(TylooCoordinatorInterceptor);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }

    public void setTransactionConfigurator(TransactionConfigurator transactionConfigurator) {
        this.transactionConfigurator = transactionConfigurator;
    }
}
