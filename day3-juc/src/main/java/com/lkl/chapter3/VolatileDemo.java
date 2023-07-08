package com.lkl.chapter3;

import java.util.concurrent.TimeUnit;

public class VolatileDemo {

    private volatile int num = 0;

    public void startThread() {
        Runnable runnable = () -> {
            /*
                num变量不加volatile修饰，子线程永远不会停止，主线程修改num值
                子线程无法感知num发生变化；一旦num加上volatile修饰，只要其他线程修改num值，
                子线程立马就能感知到，程序直接结束
             */
            while (num == 0) {
            }
        };

        // 开启一个线程
        new Thread(runnable).start();
    }

    public void updateNum() {
        num = 1;
    }

    public static void main(String[] args) {
        VolatileDemo demo = new VolatileDemo();
        demo.startThread();

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // 等待一秒，主线程修改num值
        demo.updateNum();
    }

}
