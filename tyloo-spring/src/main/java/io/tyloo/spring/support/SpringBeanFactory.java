package io.tyloo.spring.support;

import io.tyloo.support.BeanFactory;
import io.tyloo.support.FactoryBuilder;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

/*
 *
 * Spring Bean ¹¤³§
 *
 * @Author:Zh1Cheung zh1cheunglq@gmail.com
 * @Date: 20:12 2019/11/11
 *
 */

public class SpringBeanFactory implements BeanFactory, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        FactoryBuilder.registerBeanFactory(this);
    }

    @Override
    public boolean isFactoryOf(Class clazz) {
        Map map = this.applicationContext.getBeansOfType(clazz);
        return map.size() > 0;
    }

    @Override
    public <T> T getBean(Class<T> var1) {
        return this.applicationContext.getBean(var1);
    }
}
