package io.tyloo.tcctransaction.repository.helper;

import io.tyloo.tcctransaction.common.TylooTransaction;
import io.tyloo.tcctransaction.serializer.ObjectSerializer;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/*
 *
 * 事务参数序列化
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 19:08 2019/12/4
 *
 */
public class TransactionSerializer {

    public static byte[] serialize(ObjectSerializer serializer, TylooTransaction tylooTransaction) {
        Map<String, Object> map = new HashMap<String, Object>();

        map.put("GLOBAL_TX_ID", tylooTransaction.getXid().getGlobalTransactionId());
        map.put("BRANCH_QUALIFIER", tylooTransaction.getXid().getBranchQualifier());
        map.put("STATUS", tylooTransaction.getStatus().getId());
        map.put("TRANSACTION_TYPE", tylooTransaction.getType().getId());
        map.put("RETRIED_COUNT", tylooTransaction.getRetriedCount());
        map.put("CREATE_TIME", tylooTransaction.getCreateTime());
        map.put("LAST_UPDATE_TIME", tylooTransaction.getLastUpdateTime());
        map.put("VERSION", tylooTransaction.getVersion());
        map.put("CONTENT", serializer.serialize(tylooTransaction));

        return serializer.serialize(map);
    }

    public static TylooTransaction deserialize(ObjectSerializer serializer, byte[] value) {

        Map<String, Object> map = (Map<String, Object>) serializer.deserialize(value);

        byte[] content = (byte[]) map.get("CONTENT");
        TylooTransaction tylooTransaction = (TylooTransaction) serializer.deserialize(content);
        tylooTransaction.resetRetriedCount((Integer) map.get("RETRIED_COUNT"));
        tylooTransaction.setLastUpdateTime((Date) map.get("LAST_UPDATE_TIME"));
        tylooTransaction.setVersion((Long) map.get("VERSION"));
        return tylooTransaction;
    }
}
