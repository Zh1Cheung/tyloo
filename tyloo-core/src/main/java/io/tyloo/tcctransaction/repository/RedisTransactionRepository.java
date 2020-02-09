package io.tyloo.tcctransaction.repository;

import io.tyloo.tcctransaction.common.TylooTransaction;
import io.tyloo.tcctransaction.exception.TransactionIOException;
import io.tyloo.tcctransaction.repository.helper.ExpandTransactionSerializer;
import io.tyloo.tcctransaction.repository.helper.RedisHelper;
import io.tyloo.tcctransaction.serializer.KryoPoolSerializer;
import io.tyloo.tcctransaction.serializer.ObjectSerializer;
import org.apache.log4j.Logger;
import io.tyloo.tcctransaction.repository.helper.JedisCallback;
import redis.clients.jedis.*;

import javax.transaction.xa.Xid;
import java.util.*;

/*
 *
 * Redis缓存事务库
 * Jedis是Redis官方推荐的Java连接开发工具
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 19:27 2019/12/4
 *
 */
public class RedisTransactionRepository extends CachableTransactionRepository {

    private static final Logger logger = Logger.getLogger(RedisTransactionRepository.class.getSimpleName());

    private JedisPool jedisPool;

    /**
     * key前缀
     */
    private String keyPrefix = "TCC:";

    private int fetchKeySize = 1000;

    private boolean isSupportScan = true;

    private boolean isForbiddenKeys = false;

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    private ObjectSerializer serializer = new KryoPoolSerializer();

    public void setSerializer(ObjectSerializer serializer) {
        this.serializer = serializer;
    }

    public int getFetchKeySize() {
        return fetchKeySize;
    }

    public void setFetchKeySize(int fetchKeySize) {
        this.fetchKeySize = fetchKeySize;
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }

    public void setJedisPool(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
        isSupportScan = RedisHelper.isSupportScanCommand(jedisPool.getResource());
        if (!isSupportScan && isForbiddenKeys) {
            throw new RuntimeException("Redis not support 'scan' command, " +
                    "and 'keys' command is forbidden, " +
                    "try update redis version higher than 2.8.0 " +
                    "or set 'isForbiddenKeys' to false");
        }
    }

    public void setSupportScan(boolean isSupportScan) {
        this.isSupportScan = isSupportScan;
    }

    public void setForbiddenKeys(boolean forbiddenKeys) {
        isForbiddenKeys = forbiddenKeys;
    }

    @Override
    protected int doCreate(final TylooTransaction tylooTransaction) {


        try {
            Long statusCode = RedisHelper.execute(jedisPool, new JedisCallback<Long>() {

                @Override
                public Long doInJedis(Jedis jedis) {

                    //当前事务的所有参数集合
                    List<byte[]> params = new ArrayList<byte[]>();

                    for (Map.Entry<byte[], byte[]> entry : ExpandTransactionSerializer.serialize(serializer, tylooTransaction).entrySet()) {
                        params.add(entry.getKey());
                        params.add(entry.getValue());
                    }

                    Object result = jedis.eval("if redis.call('exists', KEYS[1]) == 0 then redis.call('hmset', KEYS[1], unpack(ARGV)); return 1; end; return 0;".getBytes(),
                            Arrays.asList(RedisHelper.getRedisKey(keyPrefix, tylooTransaction.getXid())), params);

                    return (Long) result;
                }
            });
            return statusCode.intValue();

        } catch (Exception e) {
            throw new TransactionIOException(e);
        }
    }

    @Override
    protected int doUpdate(final TylooTransaction tylooTransaction) {

        try {

            Long statusCode = RedisHelper.execute(jedisPool, new JedisCallback<Long>() {
                @Override
                public Long doInJedis(Jedis jedis) {

                    tylooTransaction.updateTime();
                    tylooTransaction.updateVersion();

                    List<byte[]> params = new ArrayList<byte[]>();

                    for (Map.Entry<byte[], byte[]> entry : ExpandTransactionSerializer.serialize(serializer, tylooTransaction).entrySet()) {
                        params.add(entry.getKey());
                        params.add(entry.getValue());
                    }

                    Object result = jedis.eval(String.format("if redis.call('hget',KEYS[1],'VERSION') == '%s' then redis.call('hmset', KEYS[1], unpack(ARGV)); return 1; end; return 0;",
                            tylooTransaction.getVersion() - 1).getBytes(),
                            Arrays.asList(RedisHelper.getRedisKey(keyPrefix, tylooTransaction.getXid())), params);

                    return (Long) result;
                }
            });

            return statusCode.intValue();
        } catch (Exception e) {
            throw new TransactionIOException(e);
        }
    }

    @Override
    protected int doDelete(final TylooTransaction tylooTransaction) {
        try {

            Long result = RedisHelper.execute(jedisPool, new JedisCallback<Long>() {
                @Override
                public Long doInJedis(Jedis jedis) {

                    return jedis.del(RedisHelper.getRedisKey(keyPrefix, tylooTransaction.getXid()));
                }
            });

            return result.intValue();
        } catch (Exception e) {
            throw new TransactionIOException(e);
        }
    }

    @Override
    protected TylooTransaction doFindOne(final Xid xid) {

        try {
            Long startTime = System.currentTimeMillis();
            Map<byte[], byte[]> content = RedisHelper.execute(jedisPool, new JedisCallback<Map<byte[], byte[]>>() {
                @Override
                public Map<byte[], byte[]> doInJedis(Jedis jedis) {
                    return jedis.hgetAll(RedisHelper.getRedisKey(keyPrefix, xid));
                }
            });
            logger.info("redis find cost time :" + (System.currentTimeMillis() - startTime));

            if (content != null && content.size() > 0) {
                return ExpandTransactionSerializer.deserialize(serializer, content);
            }
            return null;
        } catch (Exception e) {
            throw new TransactionIOException(e);
        }
    }

    @Override
    protected List<TylooTransaction> doFindAllUnmodifiedSince(Date date) {

        List<TylooTransaction> allTylooTransactions = doFindAll();

        List<TylooTransaction> allUnmodifiedSince = new ArrayList<TylooTransaction>();

        for (TylooTransaction tylooTransaction : allTylooTransactions) {
            if (tylooTransaction.getLastUpdateTime().compareTo(date) < 0) {
                allUnmodifiedSince.add(tylooTransaction);
            }
        }

        return allUnmodifiedSince;
    }

    /**
     * 此处用到了模糊查询
     *
     * @return
     */
    //    @Override
    protected List<TylooTransaction> doFindAll() {

        try {

            final Set<byte[]> keys = RedisHelper.execute(jedisPool, new JedisCallback<Set<byte[]>>() {
                @Override
                public Set<byte[]> doInJedis(Jedis jedis) {

                    if (isSupportScan) {
                        List<String> allKeys = new ArrayList<String>();
                        String cursor = RedisHelper.SCAN_INIT_CURSOR;
                        ScanParams scanParams = RedisHelper.buildDefaultScanParams(keyPrefix + "*", fetchKeySize);
                        do {
                            ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
                            allKeys.addAll(scanResult.getResult());
                            cursor = scanResult.getStringCursor();
                        } while (!cursor.equals(RedisHelper.SCAN_INIT_CURSOR));

                        Set<byte[]> allKeySet = new HashSet<byte[]>();

                        for (String key : allKeys) {
                            allKeySet.add(key.getBytes());
                        }
                        logger.info(String.format("find all key by scan command with pattern:%s allKeySet.size()=%d", keyPrefix + "*", allKeySet.size()));
                        return allKeySet;
                    } else {
                        return jedis.keys((keyPrefix + "*").getBytes());
                    }

                }
            });


            return RedisHelper.execute(jedisPool, new JedisCallback<List<TylooTransaction>>() {
                @Override
                public List<TylooTransaction> doInJedis(Jedis jedis) {

                    Pipeline pipeline = jedis.pipelined();

                    for (final byte[] key : keys) {
                        pipeline.hgetAll(key);
                    }
                    List<Object> result = pipeline.syncAndReturnAll();

                    List<TylooTransaction> list = new ArrayList<TylooTransaction>();
                    for (Object data : result) {

                        if (data != null && ((Map<byte[], byte[]>) data).size() > 0) {

                            list.add(ExpandTransactionSerializer.deserialize(serializer, (Map<byte[], byte[]>) data));
                        }

                    }

                    return list;
                }
            });

        } catch (Exception e) {
            throw new TransactionIOException(e);
        }
    }
}
