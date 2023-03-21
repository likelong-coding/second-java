package com.lkl;

import com.lkl.utils.JedisConnectionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;

/**
 * @author likelong
 * @date 2023/3/20
 */
public class JedisTest {

    private Jedis jedis;

    @Before
    public void setUp() {
        // 建立连接
        jedis = JedisConnectionFactory.getJedis();

        // 选择库
        jedis.select(1);
    }

    @Test
    public void testString() {
        // 插入数据，方法名称就是redis命令名称
        String result = jedis.set("name", "张三");
        System.out.println("result = " + result);
        // 获取数据
        String name = jedis.get("name");
        System.out.println("name = " + name);
    }

    @After
    public void tearDown() {
        // 释放资源
        if (jedis != null) {
            jedis.close();
        }
    }

}
