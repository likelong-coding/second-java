package com.lkl.chapter3;

import java.util.concurrent.atomic.AtomicInteger;

public class AtomicDemo {
    public static void main(String[] args) {
        AtomicInteger atomicInteger = new AtomicInteger(0);

        int i = atomicInteger.get();

        boolean b = atomicInteger.compareAndSet(i, ++i);
        System.out.println(b);
        System.out.println(atomicInteger.get());
    }
}
