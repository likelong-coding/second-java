package com.hmdp.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.hmdp.constants.RedisConstants.CACHE_SHOP_TYPE_KEY;
import static com.hmdp.constants.RedisConstants.CACHE_SHOP_TYPE_TTL;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result shopTypeList() {

        // List类型获取数据
        List<String> shopTypeJson = stringRedisTemplate.opsForList().range(CACHE_SHOP_TYPE_KEY, 0, -1);
        assert shopTypeJson != null;
        if (!shopTypeJson.isEmpty()) {
            return Result.ok(
                    shopTypeJson.stream()
                            .map(typeJson -> JSONUtil.toBean(typeJson, ShopType.class))
                            .collect(Collectors.toList())
            );
        }

        List<ShopType> typeList = query().orderByAsc("sort").list();
        // List类型统一写入缓存
        stringRedisTemplate.opsForList().rightPushAll(CACHE_SHOP_TYPE_KEY,
                // 对象List转json字符串List
                typeList.stream().map(JSONUtil::toJsonStr)
                        .collect(Collectors.toList()));
        // 类型数据不会经常变化，设置缓存有效期为一天
        stringRedisTemplate.expire(CACHE_SHOP_TYPE_KEY, CACHE_SHOP_TYPE_TTL, TimeUnit.DAYS);

        return Result.ok(typeList);
    }
}
