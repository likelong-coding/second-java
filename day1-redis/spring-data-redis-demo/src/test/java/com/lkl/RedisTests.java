package com.lkl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lkl.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
class RedisTests {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    public void testString() {
        // 插入一条string类型数据
        redisTemplate.opsForValue().set("name", "李可龙");
        // 读取string类型数据
        Object name = redisTemplate.opsForValue().get("name");
        System.out.println("name = " + name);
    }

    @Test
    public void testObject() {
        // 直接插入对象
        redisTemplate.opsForValue().set("user:1", new User("likelong", 18));

        User user = (User) redisTemplate.opsForValue().get("user:1");
        System.out.println("user = " + user);
    }

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    // JSON工具
    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testStringTemplate() throws JsonProcessingException {
        // 准备对象
        User user = new User("李可龙", 18);
        // 手动序列化
        String json = mapper.writeValueAsString(user);
        // 写入一条数据到redis
        stringRedisTemplate.opsForValue().set("user:200", json);
        // 读取数据
        String val = stringRedisTemplate.opsForValue().get("user:200");
        // 反序列化
        User user1 = mapper.readValue(val, User.class);
        System.out.println("user1 = " + user1);
    }

}
