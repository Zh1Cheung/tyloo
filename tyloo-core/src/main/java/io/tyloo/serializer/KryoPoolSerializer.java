package io.tyloo.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoCallback;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;
import io.tyloo.Transaction;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/*
 *
 * @Author:Zh1Cheung zh1cheunglq@gmail.com
 * @Date: 19:29 2019/5/22
 *
 */

public class KryoPoolSerializer implements ObjectSerializer<Transaction> {


    static KryoFactory factory = () -> {
        Kryo kryo = new Kryo();
        kryo.setReferences(true);
        kryo.setRegistrationRequired(false);
        //Fix the NPE bug when deserializing Collections.
        ((Kryo.DefaultInstantiatorStrategy) kryo.getInstantiatorStrategy())
                .setFallbackInstantiatorStrategy(new StdInstantiatorStrategy());

        return kryo;
    };


    KryoPool pool = new KryoPool.Builder(factory).softReferences().build();

    private int initPoolSize = 300;

    public KryoPoolSerializer() {
        init();
    }

    public KryoPoolSerializer(int initPoolSize) {
        this.initPoolSize = initPoolSize;
        init();
    }

    private void init() {

        for (int i = 0; i < initPoolSize; i++) {
            Kryo kryo = pool.borrow();
            pool.release(kryo);
        }
    }

    @Override
    public byte[] serialize(final Transaction object) {

        return pool.run(kryo -> {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Output output = new Output(byteArrayOutputStream);

            kryo.writeClassAndObject(output, object);
            output.flush();

            return byteArrayOutputStream.toByteArray();
        });
    }

    @Override
    public Transaction deserialize(final byte[] bytes) {

        return pool.run(kryo -> {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            Input input = new Input(byteArrayInputStream);

            return (Transaction) kryo.readClassAndObject(input);
        });
    }

    @Override
    public Transaction clone(final Transaction object) {
        return pool.run(kryo -> kryo.copy(object));
    }
}
