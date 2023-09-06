package com.lkl.chapter9;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author likelong
 * @date 2023/9/5 22:30
 * @description 周期线程调度
 */
public class ScheduledThreadPoolExecutorTest {
    public static void main(String[] args) {
        ScheduledThreadPoolExecutor threadPoolExecutor = new ScheduledThreadPoolExecutor(2);
        threadPoolExecutor.scheduleAtFixedRate(() -> System.out.println(1), 2, 3, TimeUnit.SECONDS);
    }
}
