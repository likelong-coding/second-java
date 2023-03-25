package com.hmdp.utils;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 解决缓存击穿问题——逻辑过期方案
 */
@Data
public class RedisData {

    /**
     * 逻辑过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 真正需要存到redis里的数据
     */
    private Object data;
}
