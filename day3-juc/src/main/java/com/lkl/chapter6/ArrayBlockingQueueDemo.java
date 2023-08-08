package com.lkl.chapter6;

import java.util.concurrent.ArrayBlockingQueue;

public class ArrayBlockingQueueDemo {
    public static void main(String[] args) throws InterruptedException {
        ArrayBlockingQueue fairQueue = new ArrayBlockingQueue(3, true);

        fairQueue.offer(1);
        fairQueue.offer(2);
        fairQueue.offer(3);
        fairQueue.offer(4);

        fairQueue.poll();
        System.out.println("------end-----");
    }
}
