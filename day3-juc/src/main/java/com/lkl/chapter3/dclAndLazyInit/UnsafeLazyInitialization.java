package com.lkl.chapter3.dclAndLazyInit;

import com.lkl.entity.Singleton;

public class UnsafeLazyInitialization {
    private static Singleton instance;

    /**
     * 推迟一些高开销的对象初始化操作，使用这些对象时才进行初始化。
     */
    public static Singleton getInstance() {
        if (instance == null)               // 1：A线程执行
            instance = new Singleton();     // 2：B线程执行
        return instance;
    }
}