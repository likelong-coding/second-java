package com.lkl.chapter3;

class VolatileFeaturesDemo {
    volatile long vl = 0L;

    // 单个volatile变量的写
    public void set(long l) {
        vl = l;
    }

    // 复合（多个）volatile变量的读/写
    public void getAndIncrement() {
        vl++;
    }

    // 单个volatile变量的读
    public long get() {
        return vl;
    }
}