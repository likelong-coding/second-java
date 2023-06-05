package com.lkl.utils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

public class JedisConnectionFactory {

    private static final JedisPool jedisPool;

    static {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        // 最大连接
        jedisPoolConfig.setMaxTotal(8);
        // 最大空闲连接
        jedisPoolConfig.setMaxIdle(8);
        // 最小空闲连接
        jedisPoolConfig.setMinIdle(0);
        // 设置最长等待时间 200 ms
        jedisPoolConfig.setMaxWait(Duration.ofMillis(200));
        jedisPool = new JedisPool(jedisPoolConfig, "124.222.253.33", 6379, 3000, null);
    }

    /**
     * 获取Jedis对象
     */
    public static Jedis getJedis() {
        return jedisPool.getResource();
    }
}
