package com.lkl.caseDemo;

/**
 * @author likelong
 * @date 2023/10/23 23:08
 * @description
 */
public class TestClass {

    private int m = 1;

    public int test() {
        return m + 1;
    }

    public static void main(String[] args) {
        TestClass testClass = new TestClass();
        System.out.println(testClass.test());
    }
}
