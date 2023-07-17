package com.lkl.chapter4.example;

import java.util.concurrent.TimeUnit;

public class WaitNotifyDemo {

    private static volatile boolean flag = true;

    private final static Object lock = new Object();

    static class Wait implements Runnable {
        @Override
        public void run() {
            synchronized (lock) {
                while (flag) {
                    System.out.println("1...");
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                System.out.println("3...");
            }
        }
    }

    static class Notify implements Runnable {

        @Override
        public void run() {
            synchronized (lock) {
                System.out.println("2...");
                lock.notifyAll();
                flag = false;
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            synchronized (lock) {
                System.out.println("4...");
            }
        }
    }

    public static void main(String[] args) {
        new Thread(new Wait(), "wait").start();
        new Thread(new Notify(), "notify").start();
    }
}
