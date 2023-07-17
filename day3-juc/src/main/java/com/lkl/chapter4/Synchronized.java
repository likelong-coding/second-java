package com.lkl.chapter4;

public class Synchronized {
    public static void main(String[] args) {
        // 对Synchronized Class对象进行加锁
        // 【同步块的实现使用了monitorenter和monitorexit指令】
        synchronized (Synchronized.class) {
        }
        // 静态同步方法，对Synchronized Class对象进行加锁
        m();
    }

    // 【同步方法则是依靠方法修饰符上的ACC_SYNCHRONIZED】
    public static synchronized void m() {
    }
}