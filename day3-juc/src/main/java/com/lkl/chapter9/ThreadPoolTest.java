package com.lkl.chapter9;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolTest {
    public static void main(String[] args) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                2,      // 核心线程数
                5,     // 最大线程数
                200,   // 非核心工作线程在阻塞队列位置等待的时间
                TimeUnit.SECONDS,  // 非核心工作线程在阻塞队列位置等待的单位
                new LinkedBlockingQueue<>(), // 阻塞队列，存放任务的地方
                new ThreadPoolExecutor.AbortPolicy() // 拒绝策略：这里有四种
        );

        for (int i = 0; i < 10; i++) {
            MyTask task = new MyTask();
            executor.execute(task);
        }

        // 关闭线程
        executor.shutdown();

    }
}

class MyTask implements Runnable {
    @Override
    public void run() {
        System.out.println("我被执行了....");
    }
}
