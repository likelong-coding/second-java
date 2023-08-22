package com.lkl.chapter7;

import java.util.concurrent.atomic.LongAdder;

/**
 * @author: likelong
 * @date: 2023/8/21 15:34
 * @description: LongAddr基本使用，基本原理：https://blog.csdn.net/liulianglin/article/details/126361550
 * Cell[] cells 数组 以及 volatile long base
 */
public class LongAdderTest {

    public static void main(String[] args) throws InterruptedException {
        LongAdder adder = new LongAdder();
        int[] num = new int[1];

        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 10000; j++) {
                    adder.add(1);
                    num[0] += 1;
                }
            });
            threads[i].start();
        }
        for (int i = 0; i < 10; i++) {
            threads[i].join();
        }
        // 通过对比发现，使用LongAdder能保证最后的值是期望的值，不存在并发写错误
        System.out.println("adder:" + adder);
        System.out.println("num:" + num[0]);
    }

}
