package io.tyloo.core.repository.helper;

import com.alibaba.fastjson.JSON;
import io.tyloo.api.Enums.TransactionStatus;
import io.tyloo.api.common.TylooTransaction;
import io.tyloo.core.exception.SystemException;
import io.tyloo.core.utils.ByteUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import io.tyloo.core.serializer.ObjectSerializer;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/*
 *
 * 事务参数序列化
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 19:07 2019/12/4
 *
 */
public class ExpandTransactionSerializer {

    public static Map<byte[], byte[]> serialize(ObjectSerializer serializer, TylooTransaction tylooTransaction) {

        Map<byte[], byte[]> map = new HashMap<byte[], byte[]>();

        map.put("GLOBAL_TX_ID".getBytes(), tylooTransaction.getXid().getGlobalTransactionId());
        map.put("BRANCH_QUALIFIER".getBytes(), tylooTransaction.getXid().getBranchQualifier());
        map.put("STATUS".getBytes(), ByteUtils.intToBytes(tylooTransaction.getTransactionStatus().getId()));
        map.put("TRANSACTION_TYPE".getBytes(), ByteUtils.intToBytes(tylooTransaction.getTransactionType().getId()));
        map.put("RETRIED_COUNT".getBytes(), ByteUtils.intToBytes(tylooTransaction.getRetriedCount()));
        map.put("CREATE_TIME".getBytes(), DateFormatUtils.format(tylooTransaction.getCreateTime(), "yyyy-MM-dd HH:mm:ss").getBytes());
        map.put("LAST_UPDATE_TIME".getBytes(), DateFormatUtils.format(tylooTransaction.getLastUpdateTime(), "yyyy-MM-dd HH:mm:ss").getBytes());
        map.put("VERSION".getBytes(), ByteUtils.longToBytes(tylooTransaction.getVersion()));
        map.put("CONTENT".getBytes(), serializer.serialize(tylooTransaction));
        map.put("CONTENT_VIEW".getBytes(), JSON.toJSONString(tylooTransaction).getBytes());
        return map;
    }

    public static TylooTransaction deserialize(ObjectSerializer serializer, Map<byte[], byte[]> map1) {
        //事务参数列表
        Map<String, byte[]> propertyMap = new HashMap<String, byte[]>();

        for (Map.Entry<byte[], byte[]> entry : map1.entrySet()) {
            propertyMap.put(new String(entry.getKey()), entry.getValue());
        }

        byte[] content = propertyMap.get("CONTENT");
        TylooTransaction tylooTransaction = (TylooTransaction) serializer.deserialize(content);
        tylooTransaction.changeStatus(TransactionStatus.valueOf(ByteUtils.bytesToInt(propertyMap.get("STATUS"))));
        tylooTransaction.resetRetriedCount(ByteUtils.bytesToInt(propertyMap.get("RETRIED_COUNT")));

        try {
            tylooTransaction.setLastUpdateTime(DateUtils.parseDate(new String(propertyMap.get("LAST_UPDATE_TIME")), "yyyy-MM-dd HH:mm:ss"));
        } catch (ParseException e) {
            throw new SystemException(e);
        }

        tylooTransaction.setVersion(ByteUtils.bytesToLong(propertyMap.get("VERSION")));
        return tylooTransaction;
    }
}
