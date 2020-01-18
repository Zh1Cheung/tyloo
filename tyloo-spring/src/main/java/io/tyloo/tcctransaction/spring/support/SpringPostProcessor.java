package io.tyloo.tcctransaction.spring.support;

import io.tyloo.tcctransaction.support.BeanFactory;
import io.tyloo.tcctransaction.support.FactoryBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/*
 *
 * Spring 后置处理器
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 20:13 2019/12/4
 *
 */
public class SpringPostProcessor implements ApplicationListener {
    /**
     * Spring启动时加载.
     */
    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        ContextRefreshedEvent contextRefreshedEvent = new ContextRefreshedEvent((ApplicationContext) applicationEvent);
        ApplicationContext applicationContext = contextRefreshedEvent.getApplicationContext();

        if (applicationContext.getParent() == null) {
            FactoryBuilder.registerBeanFactory(applicationContext.getBean(BeanFactory.class));
        }
    }
}
