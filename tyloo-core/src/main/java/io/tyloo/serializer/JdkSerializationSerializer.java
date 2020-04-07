package io.tyloo.serializer;

import io.tyloo.Transaction;
import org.apache.commons.lang3.SerializationUtils;

/*
 *
 * @Author:Zh1Cheung zh1cheunglq@gmail.com
 * @Date: 19:29 2019/5/22
 *
 */

public class JdkSerializationSerializer implements ObjectSerializer<Transaction> {

    @Override
    public byte[] serialize(Transaction object) {
        return SerializationUtils.serialize(object);
    }

    @Override
    public Transaction deserialize(byte[] bytes) {
        if (bytes == null) {
            return null;
        } else {
            return (Transaction) SerializationUtils.deserialize(bytes);
        }
    }

    @Override
    public Transaction clone(Transaction object) {
        return SerializationUtils.clone(object);
    }
}
