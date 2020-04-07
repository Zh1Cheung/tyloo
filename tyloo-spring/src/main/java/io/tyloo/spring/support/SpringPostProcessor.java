package io.tyloo.spring.support;

import io.tyloo.support.BeanFactory;
import io.tyloo.support.FactoryBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/*
 *
 * Spring 后置处理器
 *
 * @Author:Zh1Cheung zh1cheunglq@gmail.com
 * @Date: 20:13 2019/11/18
 *
 */

public class SpringPostProcessor implements ApplicationListener<ContextRefreshedEvent> {
    /**
     * Spring启动时加载.
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        ApplicationContext applicationContext = contextRefreshedEvent.getApplicationContext();

        if (applicationContext.getParent() == null) {
            FactoryBuilder.registerBeanFactory(applicationContext.getBean(BeanFactory.class));
        }
    }
}
