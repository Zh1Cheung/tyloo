package io.tyloo.support;

/*
 *
 * Bean¹¤³§
 *
 * @Author:Zh1Cheung zh1cheunglq@gmail.com
 * @Date: 19:34 2019/5/27
 *
 */

public interface BeanFactory {
    <T> T getBean(Class<T> var1);

    <T> boolean isFactoryOf(Class<T> clazz);
}
