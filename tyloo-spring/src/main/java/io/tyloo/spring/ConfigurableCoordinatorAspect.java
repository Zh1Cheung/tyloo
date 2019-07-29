package io.tyloo.spring;

import io.tyloo.interceptor.TylooCoordinatorAspect;
import io.tyloo.interceptor.TylooCoordinatorInterceptor;
import io.tyloo.support.TransactionConfigurator;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;

/*
 *
 * 可配置的资源协调者切面
 *
 * @Author:Zh1Cheung zh1cheunglq@gmail.com
 * @Date: 20:21 2019/10/4
 *
 */

@Aspect
public class ConfigurableCoordinatorAspect extends TylooCoordinatorAspect implements Ordered {

    private TransactionConfigurator transactionConfigurator;

    /**
     * 初始化
     * 往拦截器中注入 TylooTransactionManager
     */
    public void init() {

        TylooCoordinatorInterceptor tylooCoordinatorInterceptor = new TylooCoordinatorInterceptor();
        tylooCoordinatorInterceptor.setTransactionManager(transactionConfigurator.getTransactionManager());
        this.setTylooCoordinatorInterceptor(tylooCoordinatorInterceptor);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }

    public void setTransactionConfigurator(TransactionConfigurator transactionConfigurator) {
        this.transactionConfigurator = transactionConfigurator;
    }
}
