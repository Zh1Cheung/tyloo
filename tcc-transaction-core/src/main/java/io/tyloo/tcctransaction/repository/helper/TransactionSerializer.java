package io.tyloo.tcctransaction.repository.helper;

import io.tyloo.tcctransaction.Transaction;
import io.tyloo.tcctransaction.serializer.ObjectSerializer;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/*
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 19:08 2019/12/4
 *
 */
public class TransactionSerializer {

    public static byte[] serialize(ObjectSerializer serializer, Transaction transaction) {
        Map<String, Object> map = new HashMap<String, Object>();

        map.put("GLOBAL_TX_ID", transaction.getXid().getGlobalTransactionId());
        map.put("BRANCH_QUALIFIER", transaction.getXid().getBranchQualifier());
        map.put("STATUS", transaction.getStatus().getId());
        map.put("TRANSACTION_TYPE", transaction.getTransactionType().getId());
        map.put("RETRIED_COUNT", transaction.getRetriedCount());
        map.put("CREATE_TIME", transaction.getCreateTime());
        map.put("LAST_UPDATE_TIME", transaction.getLastUpdateTime());
        map.put("VERSION", transaction.getVersion());
        map.put("CONTENT", serializer.serialize(transaction));

        return serializer.serialize(map);
    }

    public static Transaction deserialize(ObjectSerializer serializer, byte[] value) {

        Map<String, Object> map = (Map<String, Object>) serializer.deserialize(value);

        byte[] content = (byte[]) map.get("CONTENT");
        Transaction transaction = (Transaction) serializer.deserialize(content);
        transaction.resetRetriedCount((Integer) map.get("RETRIED_COUNT"));
        transaction.setLastUpdateTime((Date) map.get("LAST_UPDATE_TIME"));
        transaction.setVersion((Long) map.get("VERSION"));
        return transaction;
    }
}
