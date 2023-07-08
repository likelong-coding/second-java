package com.lkl.chapter3;

import java.util.concurrent.locks.ReentrantLock;

class ReentrantLockExample {
    int a = 0;
    // 公平锁
    ReentrantLock lock = new ReentrantLock(true);

    public void writer() {
        // 获取锁
        lock.lock();
        try {
            a++;
        } finally {
            // 释放锁
            lock.unlock();
        }
    }

    public void reader() {
        // 获取锁
        lock.lock();
        try {
            int i = a;
            // ...
        } finally {
            // 释放锁
            lock.unlock();
        }
    }
}