package io.tyloo.unit.test;

import io.tyloo.Transaction;
import io.tyloo.serializer.JacksonJsonSerializer;
import io.tyloo.unittest.client.TransferService;
import io.tyloo.unittest.entity.SubAccount;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;


public class ReflectionTest extends AbstractTestCase {

    @Autowired
    private TransferService transferService;

    @Test
    public void test1() throws NoSuchMethodException {

        transferService.performenceTuningTransfer();

        Method transferMethod = TransferService.class.getMethod("transfer", long.class, long.class, int.class);


    }

    @Test
    public void testJacksonSerializer() {

        String json = "[\"Transaction\",{\"xid\":[\"TransactionXid\",{\"formatId\":1,\"globalTransactionId\":\"TQ5/1W2USTWZHjwV7JajxA==\",\"branchQualifier\":\"Kcjo0GaHTc6hIPpf3B7ARA==\"}],\"status\":\"TRYING\",\"transactionType\":\"ROOT\",\"retriedCount\":0,\"createTime\":[\"java.util.Date\",1575457808693],\"lastUpdateTime\":[\"java.util.Date\",1575457822864],\"version\":2,\"participants\":[\"java.util.ArrayList\",[[\"Participant\",{\"xid\":[\"TransactionXid\",{\"formatId\":1,\"globalTransactionId\":\"TQ5/1W2USTWZHjwV7JajxA==\",\"branchQualifier\":\"H1KzapA5SL+L2mu3D5tyRw==\"}],\"confirmInvocationContext\":[\"InvocationContext\",{\"targetClass\":\"TransferService\",\"methodName\":\"transferConfirm\",\"parameterTypes\":[\"long\",\"long\",\"int\"],\"args\":[\"[Ljava.lang.Object;\",[[\"java.lang.Long\",1],[\"java.lang.Long\",2],50]]}],\"cancelInvocationContext\":[\"InvocationContext\",{\"targetClass\":\"TransferService\",\"methodName\":\"transferCancel\",\"parameterTypes\":[\"long\",\"long\",\"int\"],\"args\":[\"[Ljava.lang.Object;\",[[\"java.lang.Long\",1],[\"java.lang.Long\",2],50]]}]}]]],\"attachments\":[\"java.util.concurrent.ConcurrentHashMap\",{}]}]";

        JacksonJsonSerializer jacksonJsonSerializer = new JacksonJsonSerializer();

        Transaction transaction = jacksonJsonSerializer.deserialize(json.getBytes());

        SubAccount subAccount = new SubAccount(1l, 10);

        byte[] bytes = jacksonJsonSerializer.serialize(transaction);

        json = new String(bytes);

        transaction = jacksonJsonSerializer.deserialize(bytes);

        Assert.assertTrue(transaction != null);
    }

}
