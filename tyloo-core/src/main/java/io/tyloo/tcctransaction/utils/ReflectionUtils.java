package io.tyloo.tcctransaction.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Map;

/*
 *
 * 反射工具类
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 19:50 2019/12/4
 *
 */
public class ReflectionUtils {


    /**
     * 判断一个属性是否可以访问
     *
     * @param method
     */
    public static void makeAccessible(Method method) {
        if ((!Modifier.isPublic(method.getModifiers()) || !Modifier.isPublic(method.getDeclaringClass().getModifiers())) && !method.isAccessible()) {
            method.setAccessible(true);
        }
    }

    /**
     * 运行时改变注解值
     *
     * @param annotation
     * @param key
     * @param newValue
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public static void changeAnnotationValue(Annotation annotation, String key, Object newValue) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Object handler = Proxy.getInvocationHandler(annotation);

        Field f;

        f = handler.getClass().getDeclaredField("memberValues");

        f.setAccessible(true);

        Map<String, Object> memberValues;

        memberValues = (Map<String, Object>) f.get(handler);

        Object oldValue = memberValues.get(key);

        if (oldValue == null || oldValue.getClass() != newValue.getClass()) {

            throw new IllegalArgumentException();
        }

        memberValues.put(key, newValue);

    }

    /**
     * 返回方法所在的类名
     *
     * @param aClass
     * @param methodName
     * @param parameterTypes
     * @return
     */
    public static Class getDeclaringType(Class aClass, String methodName, Class<?>[] parameterTypes) {

        Method method = null;


        Class findClass = aClass;

        do {
            Class[] clazzes = findClass.getInterfaces();

            for (Class clazz : clazzes) {

                try {
                    method = clazz.getDeclaredMethod(methodName, parameterTypes);
                } catch (NoSuchMethodException e) {
                    method = null;
                }

                if (method != null) {
                    return clazz;
                }
            }

            findClass = findClass.getSuperclass();

        } while (!findClass.equals(Object.class));

        return aClass;
    }

    /**
     * 处理基本类型
     *
     * @param type
     * @return
     */
    public static Object getNullValue(Class type) {

        if (boolean.class.equals(type)) {
            return false;
        } else if (byte.class.equals(type)) {
            return 0;
        } else if (short.class.equals(type)) {
            return 0;
        } else if (int.class.equals(type)) {
            return 0;
        } else if (long.class.equals(type)) {
            return 0;
        } else if (float.class.equals(type)) {
            return 0;
        } else if (double.class.equals(type)) {
            return 0;
        } else if (char.class.equals(type)) {
            return ' ';
        }

        return null;
    }
}
