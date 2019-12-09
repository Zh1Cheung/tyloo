package io.tyloo.tcctransaction.repository.helper;

import com.alibaba.fastjson.JSON;
import io.tyloo.tcctransaction.SystemException;
import io.tyloo.tcctransaction.Transaction;
import io.tyloo.tcctransaction.utils.ByteUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import io.tyloo.api.TransactionStatus;
import io.tyloo.tcctransaction.serializer.ObjectSerializer;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/*
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 19:07 2019/12/4
 *
 */
public class ExpandTransactionSerializer {

    public static Map<byte[], byte[]> serialize(ObjectSerializer serializer, Transaction transaction) {

        Map<byte[], byte[]> map = new HashMap<byte[], byte[]>();

        map.put("GLOBAL_TX_ID".getBytes(), transaction.getXid().getGlobalTransactionId());
        map.put("BRANCH_QUALIFIER".getBytes(), transaction.getXid().getBranchQualifier());
        map.put("STATUS".getBytes(), ByteUtils.intToBytes(transaction.getStatus().getId()));
        map.put("TRANSACTION_TYPE".getBytes(), ByteUtils.intToBytes(transaction.getTransactionType().getId()));
        map.put("RETRIED_COUNT".getBytes(), ByteUtils.intToBytes(transaction.getRetriedCount()));
        map.put("CREATE_TIME".getBytes(), DateFormatUtils.format(transaction.getCreateTime(), "yyyy-MM-dd HH:mm:ss").getBytes());
        map.put("LAST_UPDATE_TIME".getBytes(), DateFormatUtils.format(transaction.getLastUpdateTime(), "yyyy-MM-dd HH:mm:ss").getBytes());
        map.put("VERSION".getBytes(), ByteUtils.longToBytes(transaction.getVersion()));
        map.put("CONTENT".getBytes(), serializer.serialize(transaction));
        map.put("CONTENT_VIEW".getBytes(), JSON.toJSONString(transaction).getBytes());
        return map;
    }

    public static Transaction deserialize(ObjectSerializer serializer, Map<byte[], byte[]> map1) {

        Map<String, byte[]> propertyMap = new HashMap<String, byte[]>();

        for (Map.Entry<byte[], byte[]> entry : map1.entrySet()) {
            propertyMap.put(new String(entry.getKey()), entry.getValue());
        }

        byte[] content = propertyMap.get("CONTENT");
        Transaction transaction = (Transaction) serializer.deserialize(content);
        transaction.changeStatus(TransactionStatus.valueOf(ByteUtils.bytesToInt(propertyMap.get("STATUS"))));
        transaction.resetRetriedCount(ByteUtils.bytesToInt(propertyMap.get("RETRIED_COUNT")));

        try {
            transaction.setLastUpdateTime(DateUtils.parseDate(new String(propertyMap.get("LAST_UPDATE_TIME")), "yyyy-MM-dd HH:mm:ss"));
        } catch (ParseException e) {
            throw new SystemException(e);
        }

        transaction.setVersion(ByteUtils.bytesToLong(propertyMap.get("VERSION")));
        return transaction;
    }
}
