package io.tyloo.repository.helper;

import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.exceptions.JedisDataException;

import javax.transaction.xa.Xid;

/*
 *
 * Redis工具类
 *
 * @Author:Zh1Cheung zh1cheunglq@gmail.com
 * @Date: 19:07 2019/5/6
 *
 */

public class RedisHelper {

    public static int SCAN_COUNT = 30;
    public static String SCAN_TEST_PATTERN = "*";
    public static String SCAN_INIT_CURSOR = "0";

    private static final Logger logger = Logger.getLogger(RedisHelper.class);

    public static byte[] getRedisKey(String keyPrefix, Xid xid) {
        return (keyPrefix + xid.toString()).getBytes();
    }

    public static byte[] getRedisKey(String keyPrefix, String globalTransactionId, String branchQualifier) {
        return (keyPrefix + globalTransactionId + ":" + branchQualifier).getBytes();
    }

    /**
     * 对JedisPool里的每个Jedis处理
     *  @param <T>
     * @param jedisPool
     * @param callback
     * @return
     */
    public static <T> T execute(JedisPool jedisPool, JedisCallback<T> callback) {
        try (Jedis jedis = jedisPool.getResource()) {
            return callback.doInJedis(jedis);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * SCAN cursor [MATCH pattern] [COUNT count]
     * $redis-cli scan 0 match key99* count 1000
     * 1) "13912"
     * 2)  1) "key997"
     * 2) "key9906"
     * 3) "key9957"
     * 4) "key9902"
     * 5) "key9971"
     * 6) "key9935"
     * 7) "key9958"
     * 8) "key9928"
     * 9) "key9931"
     * 10) "key9961"
     * 11) "key9948"
     * 12) "key9965"
     * 13) "key9937"
     * <p>
     * $redis-cli scan 13912 match key99* count 1000
     * 1) "5292"
     * 2)  1) "key996"
     * 2) "key9960"
     * 3) "key9973"
     * 4) "key9978"
     * 5) "key9927"
     * 6) "key995"
     * 7) "key9992"
     * 8) "key9993"
     * 9) "key9964"
     * 10) "key9934"
     * 返回结果分为两个部分：第一部分即 1) 就是下一次迭代游标，第二部分即 2) 就是本次迭代结果集。
     *
     * @param pattern
     * @param count
     * @return
     */
    public static ScanParams buildDefaultScanParams(String pattern, int count) {
        return new ScanParams().match(pattern).count(count);
    }

    /**
     * 是否支持scan命令（Jedis）
     *
     * @param jedis
     * @return
     */
    public static Boolean isSupportScanCommand(Jedis jedis) {
        try {
            ScanParams scanParams = buildDefaultScanParams(SCAN_TEST_PATTERN, SCAN_COUNT);
            jedis.scan(SCAN_INIT_CURSOR, scanParams);
        } catch (JedisDataException e) {
            logger.error(e.getMessage(), e);
            logger.info("Redis **NOT** support scan command");
            return false;
        }

        logger.info("Redis support scan command");
        return true;
    }
}