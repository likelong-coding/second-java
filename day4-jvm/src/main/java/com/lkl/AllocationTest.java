package com.lkl;

/**
 * @author likelong
 * @date 2023/9/27 23:25
 * @description VM options -Xms20M -Xmx20M -Xmn10M -XX:SurvivorRatio=8 -XX:+PrintGCDetails
 */
public class AllocationTest {

    private static final int _1MB = 1024 * 1024;

    public static void main(String[] args) {

        byte[] a1, a2, a3, a4;
        a1 = new byte[2 * _1MB];
        a2 = new byte[2 * _1MB];
        a3 = new byte[2 * _1MB];
        a4 = new byte[4 * _1MB]; // 出现一次Minor GC

    }
}
