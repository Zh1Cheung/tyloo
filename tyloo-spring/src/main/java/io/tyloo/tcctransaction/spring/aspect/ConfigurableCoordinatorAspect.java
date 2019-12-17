package io.tyloo.tcctransaction.spring.aspect;

import org.aspectj.lang.annotation.Aspect;
import io.tyloo.tcctransaction.interceptor.ResourceCoordinatorAspect;
import io.tyloo.tcctransaction.interceptor.ResourceCoordinatorInterceptor;
import io.tyloo.tcctransaction.support.TransactionConfigurator;
import org.springframework.core.Ordered;

/*
 *
 * 可配置的资源协调者切面
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 20:21 2019/12/4
 *
 */
@Aspect
public class ConfigurableCoordinatorAspect extends ResourceCoordinatorAspect implements Ordered {

    private TransactionConfigurator transactionConfigurator;

    /**
     * 初始化
     * 往拦截器中注入 TransactionManager
     */
    public void init() {

        ResourceCoordinatorInterceptor resourceCoordinatorInterceptor = new ResourceCoordinatorInterceptor();
        resourceCoordinatorInterceptor.setTransactionManager(transactionConfigurator.getTransactionManager());
        this.setResourceCoordinatorInterceptor(resourceCoordinatorInterceptor);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }

    public void setTransactionConfigurator(TransactionConfigurator transactionConfigurator) {
        this.transactionConfigurator = transactionConfigurator;
    }
}
