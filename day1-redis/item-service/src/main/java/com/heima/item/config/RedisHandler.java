package com.heima.item.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heima.item.pojo.Item;
import com.heima.item.pojo.ItemStock;
import com.heima.item.service.IItemService;
import com.heima.item.service.impl.ItemStockService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author likelong
 * @date 2023/5/21
 */
@Component
public class RedisHandler implements InitializingBean {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private IItemService itemService;

    @Resource
    private ItemStockService itemStockService;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * redis缓存预热
     */
    @Override
    public void afterPropertiesSet() throws Exception {

        List<Item> itemList = itemService.list();
        for (Item item : itemList) {
            String json = MAPPER.writeValueAsString(item);
            stringRedisTemplate.opsForValue().set("item:id:" + item.getId(), json);
        }

        List<ItemStock> stockList = itemStockService.list();
        for (ItemStock stock : stockList) {
            String json = MAPPER.writeValueAsString(stock);
            stringRedisTemplate.opsForValue().set("item:stock:id:" + stock.getId(), json);
        }

    }
}
