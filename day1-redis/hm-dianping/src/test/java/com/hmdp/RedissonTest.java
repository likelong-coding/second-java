package com.hmdp;

import com.hmdp.utils.SimpleRedisLock;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author likelong
 * @date 2023/3/31
 */
@Slf4j
@SpringBootTest
class RedissonTest {

    @Autowired
    private RedissonClient redissonClient;
    // 可重入锁
    private RLock lock;

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    // 不可重入锁
    private SimpleRedisLock simpleRedisLock;

    @BeforeEach
    public void setUp() {
        lock = redissonClient.getLock("order");
        simpleRedisLock = new SimpleRedisLock("simple", stringRedisTemplate);
    }

    @Test
    public void method1() throws InterruptedException {
        boolean isLock = lock.tryLock(1, TimeUnit.SECONDS);
        if (!isLock) {
            log.error("获取锁失败，1");
            return;
        }
        try {
            log.info("获取锁成功，1");
            method2();
        } finally {
            log.info("释放锁，1");
            lock.unlock();
        }
    }

    public void method2() {
        boolean isLock = lock.tryLock();
        if (!isLock) {
            log.error("获取锁失败，2");
            return;
        }
        try {
            log.info("获取锁成功，2");
        } finally {
            log.info("释放锁，2");
            lock.unlock();
        }
    }

}
