package com.lkl;

/**
 * @author likelong
 * @date 2023/9/19 22:55
 * @description -Xss128k
 */
public class StackOverFlowDemo {

    private static int stackLen = 1;

    public void stackLeak() {
        stackLen++;
        stackLeak();
    }

    public static void main(String[] args) {
        StackOverFlowDemo demo = new StackOverFlowDemo();
        try {
            demo.stackLeak();
        } catch (Throwable e) {
            System.out.println("stack len " + stackLen);
            throw e;
        }
    }
}
