package com.hmdp;

import com.hmdp.utils.SimpleRedisLock;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;

/**
 * @author likelong
 * @date 2023/3/31
 */
@Slf4j
@SpringBootTest
class RedissonTest {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RedissonClient redissonClient1;
    // 可重入锁
    private RLock lock1;
    private RLock lock2;

    // 联锁
    private RedissonMultiLock multiLock;

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    // 不可重入锁
    private SimpleRedisLock simpleRedisLock;

    @BeforeEach
    public void setUp() {
        lock1 = redissonClient.getLock("order");
        lock2 = redissonClient1.getLock("order");
        multiLock = new RedissonMultiLock(lock1, lock2);
        simpleRedisLock = new SimpleRedisLock("simple", stringRedisTemplate);
    }

    @Test
    public void method1() throws InterruptedException {
        boolean isLock = multiLock.tryLock();
        if (!isLock) {
            log.error("获取锁失败，1");
            return;
        }
        try {
            log.info("获取锁成功，1");
            method2();
        } finally {
            log.info("释放锁，1");
            multiLock.unlock();
        }
    }

    public void method2() {
        boolean isLock = multiLock.tryLock();
        if (!isLock) {
            log.error("获取锁失败，2");
            return;
        }
        try {
            log.info("获取锁成功，2");
        } finally {
            log.info("释放锁，2");
            multiLock.unlock();
        }
    }

}
