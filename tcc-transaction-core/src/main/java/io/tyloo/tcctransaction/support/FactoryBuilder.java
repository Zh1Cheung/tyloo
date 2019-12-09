package io.tyloo.tcctransaction.support;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/*
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 19:35 2019/12/4
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
    private static ConcurrentHashMap<Class, SingletonFactory> classFactoryMap = new ConcurrentHashMap<Class, SingletonFactory>();

    /**
     * 将Bean工厂注册到当前Builder
     *
     * @param beanFactory Bean工厂
     */
    public static void registerBeanFactory(BeanFactory beanFactory) {
        beanFactories.add(beanFactory);
    }

    /**
     * 获得指定类单例工厂
     *
     * @param clazz 指定类
     * @param <T>   泛型
     * @return 单例工厂
     */
    public static <T> SingletonFactory<T> factoryOf(Class<T> clazz) {
        if (!classFactoryMap.containsKey(clazz)) {
            // 优先从 Bean 工厂集合 获取
            for (BeanFactory beanFactory : beanFactories) {
                if (beanFactory.isFactoryOf(clazz)) {
                    classFactoryMap.putIfAbsent(clazz, new SingletonFactory<T>(clazz, beanFactory.getBean(clazz)));
                }
            }
            // 查找不到，创建 SingletonFactory
            if (!classFactoryMap.containsKey(clazz)) {
                classFactoryMap.putIfAbsent(clazz, new SingletonFactory<T>(clazz));
            }
        }
        return classFactoryMap.get(clazz);
    }

    /**
     * 单例工厂
     *
     * @param <T> 泛型
     */
    public static class SingletonFactory<T> {

        /**
         * 单例
         */
        private volatile T instance = null;
        /**
         * 类名
         */
        private String className;

        public SingletonFactory(Class<T> clazz, T instance) {
            this.className = clazz.getName();
            this.instance = instance;
        }

        public SingletonFactory(Class<T> clazz) {
            this.className = clazz.getName();
        }

        /**
         * 获得单例
         *
         * @return 单例
         */
        public T getInstance() {
            if (instance == null) { // 不存在时，创建单例
                synchronized (SingletonFactory.class) {
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
            if (this == other) return true;
            if (other == null || getClass() != other.getClass()) return false;
            SingletonFactory that = (SingletonFactory) other;
            return className.equals(that.className);
        }

        @Override
        public int hashCode() {
            return className.hashCode();
        }
    }
}