package com.lkl.chapter5;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class FairAndUnfairTest {
    private static ReentrantLock2 fairLock = new ReentrantLock2(true);
    private static ReentrantLock2 unfairLock = new ReentrantLock2(false);

    @Test
    public void fair() {
        testLock(fairLock);
    }

    @Test
    public void unfair() {
        testLock(unfairLock);
    }

    private void testLock(ReentrantLock2 lock) {
        for (int i = 0; i < 5; i++) {
            new Job(lock).start();
        }
    }

    private static class Job extends Thread {
        private final ReentrantLock2 lock;

        public Job(ReentrantLock2 lock) {
            this.lock = lock;
        }

        public void run() {
            lock.lock();
            // 连续2次打印当前的Thread和等待队列中的Thread（略）
            try {
                TimeUnit.SECONDS.sleep(2);
                List<Long> ids = lock.getQueuedThreads().stream().map(Thread::getId).collect(Collectors.toList());
                System.out.println("Lock by [" + Thread.currentThread().getId() + "], waiting by  ["
                        + ids + "]");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                List<Long> ids = lock.getQueuedThreads().stream().map(Thread::getId).collect(Collectors.toList());
                lock.unlock();
                System.out.println("Lock by [" + Thread.currentThread().getId() + "], waiting by  ["
                        + ids + "]");
            }
        }
    }

    private static class ReentrantLock2 extends ReentrantLock {
        public ReentrantLock2(boolean fair) {
            super(fair);
        }

        public Collection<Thread> getQueuedThreads() {
            List<Thread> arrayList = new ArrayList<>(super.getQueuedThreads());
            Collections.reverse(arrayList);
            return arrayList;
        }
    }
}