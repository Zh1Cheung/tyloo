package io.tyloo.tcctransaction.repository.helper;

import redis.clients.jedis.Jedis;

/*
 * Jedis回调接口
 * 在doInJedis方法中为你提供一个未封装过的 jedis 对象，可以使用原生的 jedis 的各种方法
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 19:07 2019/12/4
 *
 */
public interface JedisCallback<T> {

    public T doInJedis(Jedis jedis);
}