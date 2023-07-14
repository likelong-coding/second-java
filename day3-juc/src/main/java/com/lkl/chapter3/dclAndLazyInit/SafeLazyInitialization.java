package com.lkl.chapter3.dclAndLazyInit;

import com.lkl.entity.Singleton;

public class SafeLazyInitialization {
    private static Singleton instance;


    /**
     * 线程串行执行
     */
    public synchronized static Singleton getInstance() {
        if (instance == null)
            instance = new Singleton();
        return instance;
    }
}