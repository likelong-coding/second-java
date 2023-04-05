package com.hmdp.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author likelong
 * @date 2023/3/31
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
public class RedissonClientConfig {

    private String host;

    private int port;

    private int database;

    private String password;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        String redisAddress = String.format("redis://%s:%s", host, port);
        // 单节点redis
        config.useSingleServer().setAddress(redisAddress).setDatabase(database).setPassword(password);
        return Redisson.create(config);
    }

    @Bean
    public RedissonClient redissonClient1() {
        Config config = new Config();
        String redisAddress = String.format("redis://%s:%s", host, 6379);
        // 单节点redis
        config.useSingleServer().setAddress(redisAddress).setDatabase(database).setPassword("root");
        return Redisson.create(config);
    }
}
