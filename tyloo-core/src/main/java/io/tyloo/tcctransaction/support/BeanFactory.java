package io.tyloo.tcctransaction.support;

/*
 *
 * Bean工厂
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 19:34 2019/12/4
 *
 */
public interface BeanFactory {
    <T> T getBean(Class<T> var1);

    <T> boolean isFactoryOf(Class<T> clazz);
}
