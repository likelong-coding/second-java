package com.lkl.chapter5;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

class Mutex implements Lock {

    // 静态内部类，自定义同步器
    private static class Sync extends AbstractQueuedSynchronizer {
        // 是否处于占用状态
        protected boolean isHeldExclusively() {
            return getState() == 1;
        }

        // 当状态为0的时候获取锁
        public boolean tryAcquire(int acquires) {
            if (compareAndSetState(0, 1)) {
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        // 释放锁，将状态设置为0
        protected boolean tryRelease(int releases) {
            if (getState() == 0) throw new
                    IllegalMonitorStateException();
            setExclusiveOwnerThread(null);
            // setState 保证前面设置owner线程被其他线程可见
            setState(0);
            return true;
        }

        // 返回一个Condition，每个condition都包含了一个condition队列
        Condition newCondition() {
            return new ConditionObject();
        }
    }

    // 仅需要将操作代理到Sync上即可
    private final Sync sync = new Sync();

    // 加锁（不成功加入阻塞队列）
    public void lock() {
        sync.acquire(1);
    }

    // 尝试加锁（一次）
    public boolean tryLock() {
        return sync.tryAcquire(1);
    }

    public void unlock() {
        sync.release(1);
    }

    public Condition newCondition() {
        return sync.newCondition();
    }

    public boolean isLocked() {
        return sync.isHeldExclusively();
    }

    public boolean hasQueuedThreads() {
        return sync.hasQueuedThreads();
    }

    // 加锁，可打断
    public void lockInterruptibly() throws InterruptedException {
        sync.acquireInterruptibly(1);
    }

    // 尝试加锁有超时时间（一次）
    public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
        return sync.tryAcquireNanos(1, unit.toNanos(timeout));
    }

    public static void main(String[] args) {
        Mutex myLock = new Mutex();
        new Thread(() -> {
            myLock.lock();
            System.out.println("lock1..." + LocalDateTime.now());
//            myLock.lock();
//            System.out.println("lock2... " + LocalDateTime.now());
            try {
                System.out.println("lock... t1 " + LocalDateTime.now());
                TimeUnit.SECONDS.sleep(2);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                System.out.println("unlock... t1 " + LocalDateTime.now());
                myLock.unlock();
            }
        }, "t1").start();

        new Thread(() -> {
            myLock.lock();
            try {
                System.out.println("lock... t2 " + LocalDateTime.now());
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                System.out.println("unlock... t2 " + LocalDateTime.now());
                myLock.unlock();
            }
        }, "t2").start();
    }
}