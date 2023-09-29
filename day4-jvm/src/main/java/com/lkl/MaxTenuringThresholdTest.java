package com.lkl;

/**
 * @author likelong
 * @description VM options -Xms20M -Xmx20M -Xmn10M -XX:SurvivorRatio=8 -XX:+PrintGCDetails -XX:MaxTenuringThreshold=1
 */
public class MaxTenuringThresholdTest {

    private static final int _1MB = 1024 * 1024;

    public static void main(String[] args) {

        byte[] a1, a2, a3;
        a1 = new byte[_1MB / 4];
        a2 = new byte[4 * _1MB];
        a3 = new byte[4 * _1MB];
        a3 = null;
        a3 = new byte[4 * _1MB];
    }
}
