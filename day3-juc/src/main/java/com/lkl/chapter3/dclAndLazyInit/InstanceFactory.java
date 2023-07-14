package com.lkl.chapter3.dclAndLazyInit;

import com.lkl.entity.Singleton;

/**
 * 初始化一个类或者接口处理流程？
 */
public class InstanceFactory {

    /**
     * 类或者接口在初始化的时候，jvm会为类或者接口设置一个唯一与之对应的
     * `初始化锁`，保证类只被初始化一次
     */
    private static class InstanceHolder {
        public static Singleton instance = new Singleton();
    }

    public static Singleton getInstance() {
        // 这里将导致InstanceHolder类被初始化
        return InstanceHolder.instance;
    }
}