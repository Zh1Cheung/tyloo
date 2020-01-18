package io.tyloo.tcctransaction.spring.aspect;


import io.tyloo.tcctransaction.interceptor.TylooCoordinatorAspect;
import io.tyloo.tcctransaction.interceptor.TylooCoordinatorInterceptor;
import io.tyloo.tcctransaction.support.TransactionConfigurator;
import org.aspectj.lang.annotation.Aspect;
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
public class ConfigurableCoordinatorAspect extends TylooCoordinatorAspect implements Ordered {

    private TransactionConfigurator transactionConfigurator;

    /**
     * 初始化
     * 往拦截器中注入 TransactionManager
     */
    public void init() {

        TylooCoordinatorInterceptor resourceCoordinatorInterceptor = new TylooCoordinatorInterceptor();
        resourceCoordinatorInterceptor.setTransactionManager(transactionConfigurator.getTransactionManager());
        this.setTylooCoordinatorInterceptor(resourceCoordinatorInterceptor);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }

    public void setTransactionConfigurator(TransactionConfigurator transactionConfigurator) {
        this.transactionConfigurator = transactionConfigurator;
    }
}
