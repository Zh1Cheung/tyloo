package io.tyloo.tcctransaction.repository.helper;

import redis.clients.jedis.Jedis;

/*
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 19:07 2019/12/4
 *
 */
public interface JedisCallback<T> {

    public T doInJedis(Jedis jedis);
}