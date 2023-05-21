package com.heima.item.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.heima.item.pojo.Item;
import com.heima.item.pojo.ItemStock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author likelong
 * @date 2023/5/21
 */
@Configuration
public class CaffeineConfig {

    @Bean
    public Cache<Long, Item> itemCache() {
        return Caffeine.newBuilder()
                .initialCapacity(100).maximumSize(10_000)
                .build();
    }

    @Bean
    public Cache<Long, ItemStock> itemStockCache() {
        return Caffeine.newBuilder()
                .initialCapacity(100).maximumSize(10_000)
                .build();
    }
}
