package com.lkl.chapter3;

public class VolatileDemo1 {

    private static volatile int num = 0;

    public static void increase() {
        num++;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            // 创建一百个线程，每个线程数字+1000
            new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    increase();
                }
            }).start();
        }

        // 确保10个子线程执行完毕
        while (Thread.activeCount() > 2) { // main gc
            Thread.yield();
        }

        // 期望10000，实际上到不了
        System.out.println(num);
    }
}
