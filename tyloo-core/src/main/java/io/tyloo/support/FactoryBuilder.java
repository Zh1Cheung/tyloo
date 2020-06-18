package io.tyloo.support;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/*
 *
 * Bean工厂建造者
 *
 * @Author:Zh1Cheung zh1cheunglq@gmail.com
 * @Date: 19:35 2019/5/27
 *
 */
public final class FactoryBuilder {


    private FactoryBuilder() {

    }

    /**
     * Bean 工厂集合
     */
    private static List<BeanFactory> beanFactories = new ArrayList<BeanFactory>();

    /**
     * 类与Bean工厂 的映射
     */
    private static ConcurrentHashMap<Class, SingeltonFactory> classFactoryMap = new ConcurrentHashMap<>();

    /**
     * 获得指定类单例工厂
     *
     * @param <T>   泛型
     * @param clazz 指定类
     * @return 单例工厂
     */
    public static <T> SingeltonFactory<T> factoryOf(Class<T> clazz) {

        if (!classFactoryMap.containsKey(clazz)) {
            for (BeanFactory beanFactory : beanFactories) {
                if (beanFactory.isFactoryOf(clazz)) {
                    classFactoryMap.putIfAbsent(clazz, new SingeltonFactory<T>(clazz, beanFactory.getBean(clazz)));
                }
            }
            if (!classFactoryMap.containsKey(clazz)) {
                classFactoryMap.putIfAbsent(clazz, new SingeltonFactory<T>(clazz));
            }
        }

        return classFactoryMap.get(clazz);
    }

    /**
     * 将Bean工厂注册到当前Builder
     *
     * @param beanFactory Bean工厂
     */
    public static void registerBeanFactory(BeanFactory beanFactory) {
        beanFactories.add(beanFactory);
    }

    /**
     * 单例工厂
     *
     * @param <T> 泛型
     */
    public static class SingeltonFactory<T> {

        private volatile T instance = null;

        private String className;

        public SingeltonFactory(Class<T> clazz, T instance) {
            this.className = clazz.getName();
            this.instance = instance;
        }

        public SingeltonFactory(Class<T> clazz) {
            this.className = clazz.getName();
        }

        /**
         * 获得单例
         *
         * @return 单例
         */
        public T getInstance() {
            if (instance == null) {
                synchronized (SingeltonFactory.class) {
                    if (instance == null) {
                        try {
                            ClassLoader loader = Thread.currentThread().getContextClassLoader();
                            Class<?> clazz = loader.loadClass(className);
                            instance = (T) clazz.newInstance();
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to create an instance of " + className, e);
                        }
                    }
                }
            }

            return instance;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (other == null || getClass() != other.getClass()) {
                return false;
            }

            SingeltonFactory that = (SingeltonFactory) other;

            return className.equals(that.className);
        }

        @Override
        public int hashCode() {
            return className.hashCode();
        }
    }
}