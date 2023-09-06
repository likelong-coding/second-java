package com.lkl.chapter9;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

class MyDelayedTask implements Delayed {
    // 当前任务创建时间
    private long start = System.currentTimeMillis();
    // 延时时间
    private long time;

    // 初始化
    public MyDelayedTask(long time) {
        this.time = time;
    }

    /**
     * 需要实现的接口，获得延迟时间（用过期时间-当前时间）
     */
    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert((start + time) - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * 用于延迟队列内部比较排序（当前时间的延迟时间 - 比较对象的延迟时间）
     */
    @Override
    public int compareTo(Delayed o) {
        return (int) (this.getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS));
    }
}
