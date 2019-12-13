package io.tyloo.tcctransaction.serializer;

/*
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 19:29 2019/12/4
 *
 */
public interface ObjectSerializer<T> {


    byte[] serialize(T t);


    T deserialize(byte[] bytes);


    T clone(T object);
}
