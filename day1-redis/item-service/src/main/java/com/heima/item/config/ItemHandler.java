package com.heima.item.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.heima.item.pojo.Item;
import org.springframework.stereotype.Component;
import top.javatool.canal.client.annotation.CanalTable;
import top.javatool.canal.client.handler.EntryHandler;

import javax.annotation.Resource;

/**
 * @author likelong
 * @date 2023/5/23
 */
@CanalTable("tb_item")
@Component
public class ItemHandler implements EntryHandler<Item> {

    @Resource
    private RedisHandler redisHandler;

    @Resource
    Cache<Long, Item> itemCache;

    @Override
    public void insert(Item item) {
        // 先操作本地缓存再操作redis
        itemCache.put(item.getId(), item);
        redisHandler.saveItem(item);
    }

    @Override
    public void update(Item before, Item after) {
        itemCache.put(after.getId(), after);
        redisHandler.saveItem(after);
    }

    @Override
    public void delete(Item item) {
        itemCache.invalidate(item.getId());
        redisHandler.deleteItemById(item.getId());
    }
}
