package com.hmdp;

import com.hmdp.entity.Shop;
import com.hmdp.service.impl.ShopServiceImpl;
import com.hmdp.utils.RedisIdWorker;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.hmdp.constants.RedisConstants.SHOP_GEO_KEY;

@SpringBootTest
class HmDianPingApplicationTests {

    @Resource
    private ShopServiceImpl shopService;

    @Test
    public void testSaveShop2Redis() {
        shopService.saveShop2Redis(1L, 10L);
    }

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedisIdWorker redisIdWorker;

    private final ExecutorService es = Executors.newFixedThreadPool(500);

    /**
     * 多线程测试id生成器效果
     */
    @Test
    public void testIdWorker() throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(300);

        Runnable task = () -> {
            for (int i = 0; i < 100; i++) {
                long id = redisIdWorker.nextId("order");
                System.out.println("id = " + id);
            }

            // -1
            latch.countDown();
        };

        long begin = System.currentTimeMillis();
        // 300个线程同时跑，每个线程生成100个id
        for (int i = 0; i < 300; i++) {
            es.submit(task);
        }

        // 所有线程执行完后，打印耗时
        latch.await();
        long end = System.currentTimeMillis();
        System.out.println("time = " + (end - begin));

    }

    /**
     * 导入商铺数据
     */
    @Test
    void loadShopData() {
        // 1.查询店铺信息
        List<Shop> list = shopService.list();
        // 2.把店铺分组，按照typeId分组，typeId一致的放到一个集合
        Map<Long, List<Shop>> map = list.stream().collect(Collectors.groupingBy(Shop::getTypeId));
        // 3.分批完成写入Redis
        for (Map.Entry<Long, List<Shop>> entry : map.entrySet()) {
            // 3.1.获取类型id
            Long typeId = entry.getKey();
            String key = SHOP_GEO_KEY + typeId;
            // 3.2.获取同类型的店铺的集合
            List<Shop> value = entry.getValue();
            // 3.3.写入redis GEOADD key 经度 纬度 member
            List<RedisGeoCommands.GeoLocation<String>> locations = value.stream()
                    .map(shop -> new RedisGeoCommands.GeoLocation<>(shop.getId().toString(), new Point(shop.getX(), shop.getY())))
                    .collect(Collectors.toList());

            // 批量导入
            stringRedisTemplate.opsForGeo().add(key, locations);
        }
    }

    /**
     * 百万数据UV统计
     */
    @Test
    public void testHyperLogLog() {
        String[] users = new String[1000];
        // 数组角标
        int index = 0;
        for (int i = 1; i <= 1000000; i++) {
            // 赋值
            users[index++] = "user_" + i;
            // 每1000条发送一次
            if (i % 1000 == 0) {
                index = 0;
                stringRedisTemplate.opsForHyperLogLog().add("hll2", users);
            }

        }
        // 统计数量
        Long size = stringRedisTemplate.opsForHyperLogLog().size("hll2");
        System.out.println("size = " + size);
    }

    @Test
    public void testSetBigKey() {
        Map<String, String> map = new HashMap<>();
        for (int i = 1; i <= 650; i++) {
            map.put("hello_" + i, "world!");
        }
        stringRedisTemplate.opsForHash().putAll("m2", map);
    }

    @Test
    public void testBigHash() {
        Map<String, String> map = new HashMap<>();
        for (int i = 1; i <= 100000; i++) {
            map.put("key_" + i, "value_" + i);
        }
        stringRedisTemplate.opsForHash().putAll("test:big:hash", map);
    }

    @Test
    public void testBigString() {
        for (int i = 1; i <= 1000; i++) {
            stringRedisTemplate.opsForValue().set("test:str:key_" + i, "value_" + i);
        }
    }

    @Test
    public void testSmallHash() {
        int hashSize = 100;
        Map<String, String> map = new HashMap<>(hashSize);
        for (int i = 1; i <= 100000; i++) {
            int k = (i - 1) / hashSize;
            int v = i % hashSize;
            map.put("key_" + v, "value_" + v);
            if (v == 0) {
                stringRedisTemplate.opsForHash().putAll("test:small:hash_" + k, map);
            }
        }
    }

    @Test
    public void testMsetInCluster() {
        Map<String, String> map = new HashMap<>();
        map.put("name", "Jack");
        map.put("age", "21");
        map.put("sex", "male");
        stringRedisTemplate.opsForValue().multiSet(map);

        List<String> strings = stringRedisTemplate.opsForValue().multiGet(Arrays.asList("name", "age", "sex"));
        assert strings != null;
        strings.forEach(System.out::println);
    }
}
