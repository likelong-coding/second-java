package com.lkl.chapter4;

import com.lkl.utils.SleepUtils;

/**
 * 守护线程
 * 在构建Daemon线程时，不能依靠finally块中的内容来确保执行关闭或清理资源的逻辑。
 */
public class Daemon {
    public static void main(String[] args) {
        Thread thread = new Thread(new DaemonRunner(), "DaemonRunner");
        thread.setDaemon(true);
        thread.start();
    }

    static class DaemonRunner implements Runnable {
        @Override
        public void run() {
            try {
                SleepUtils.second(5);
            } finally {
                System.out.println("DaemonThread finally run.");
            }
        }
    }
}