package com.lkl.chapter4;

import java.util.concurrent.TimeUnit;

/**
 * ThreadLocal 以线程为key ， 泛型类型为value （线程隔离）
 */
public class ProfilerThreadLocal {
    // 第一次get()方法调用时会进行初始化（如果set方法没有调用），每个线程会调用一次
    private static final ThreadLocal<Long> TIME_THREADLOCAL = ThreadLocal.withInitial(System::currentTimeMillis);

    public static final void begin() {
        TIME_THREADLOCAL.set(System.currentTimeMillis());
    }

    public static final long end() {
        return System.currentTimeMillis() - TIME_THREADLOCAL.get();
    }

    public static void main(String[] args) throws Exception {
        ProfilerThreadLocal.begin();
        TimeUnit.SECONDS.sleep(1);
        System.out.println(Thread.currentThread().getName() + " Cost: " + ProfilerThreadLocal.end() + " mills");


        new Thread(() -> {
            ProfilerThreadLocal.begin();
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println(Thread.currentThread().getName() + " Cost: " + ProfilerThreadLocal.end() + " mills");
        }, "test-thread").start();
    }
}