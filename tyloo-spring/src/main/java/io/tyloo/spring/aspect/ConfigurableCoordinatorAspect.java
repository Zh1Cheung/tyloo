package io.tyloo.spring.aspect;


import io.tyloo.core.interceptor.TylooCoordinatorAspect;
import io.tyloo.core.interceptor.TylooCoordinatorInterceptor;
import io.tyloo.core.support.TransactionConfigurator;
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
     * 往拦截器中注入 TylooTransactionManager
     */
    public void init() {

        TylooCoordinatorInterceptor resourceCoordinatorInterceptor = new TylooCoordinatorInterceptor();
        resourceCoordinatorInterceptor.setTylooTransactionManager(transactionConfigurator.getTylooTransactionManager());
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
