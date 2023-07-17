package com.lkl.chapter4;

import com.lkl.utils.SleepUtils;

public class ThreadState {
    public static void main(String[] args) {
        /*
            Terminal查看线程状态

                第一步：jps
                20592 Launcher
                23216 Jps
                24508
                25548 ThreadState

                第二步：jstack 25548

         */
        new Thread(new TimeWaiting(), "TimeWaitingThread").start(); // TIMED_WAITING
        new Thread(new Waiting(), "WaitingThread").start(); // WAITING
        // 使用两个Blocked线程，一个获取锁成功，另一个被阻塞
        new Thread(new Blocked(), "BlockedThread-1").start(); // TIMED_WAITING
        new Thread(new Blocked(), "BlockedThread-2").start(); // BLOCKED
    }

    // 该线程不断地进行睡眠
    static class TimeWaiting implements Runnable {
        @Override
        public void run() {
            while (true) {
                SleepUtils.second(100);
            }
        }
    }

    // 该线程在Waiting.class实例上等待
    static class Waiting implements Runnable {
        @Override
        public void run() {
            while (true) {
                synchronized (Waiting.class) {
                    try {
                        Waiting.class.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // 该线程在Blocked.class实例上加锁后，不会释放该锁
    static class Blocked implements Runnable {
        public void run() {
            synchronized (Blocked.class) {
                while (true) {
                    SleepUtils.second(100);
                }
            }
        }
    }
}