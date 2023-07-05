package com.lkl.chapter2;

import com.lkl.entity.Singleton;

/**
 * @author likelong
 * @date 2023/7/5
 */
public class VolatileDemo {

    private volatile Singleton instance = new Singleton();
}
