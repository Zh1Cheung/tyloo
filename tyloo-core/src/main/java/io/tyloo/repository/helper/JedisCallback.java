package io.tyloo.repository.helper;

import redis.clients.jedis.Jedis;

/*
 * Jedis回调接口
 *
 * @Author:Zh1Cheung zh1cheunglq@gmail.com
 * @Date: 19:07 2019/5/4
 *
 */

public interface JedisCallback<T> {

    /**
     * 在doInJedis方法中为你提供一个未封装过的 jedis 对象，可以使用原生的 jedis 的各种方法
     * @param jedis
     * @return
     */
    public T doInJedis(Jedis jedis) throws CloneNotSupportedException;
}