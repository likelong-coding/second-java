package com.lkl.caseDemo;

/**
 * @author likelong
 * @date 2023/10/25 0:22
 * @description
 */
public class Test {
    void onlyMe(Foo f) {
        synchronized (f) {
            doSomething();
        }
    }

    private void doSomething() {
    }

    static class Foo {

    }
}
