package io.tyloo.tcctransaction.serializer;

import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;

/*
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 19:29 2019/12/4
 *
 */
public class JdkSerializationSerializer<T extends Serializable> implements ObjectSerializer<T> {

    @Override
    public byte[] serialize(T object) {
        return SerializationUtils.serialize(object);
    }

    @Override
    public T deserialize(byte[] bytes) {
        if (bytes == null) {
            return null;
        } else {
            return (T) SerializationUtils.deserialize(bytes);
        }
    }

    @Override
    public T clone(T object) {
        return SerializationUtils.clone(object);
    }
}
