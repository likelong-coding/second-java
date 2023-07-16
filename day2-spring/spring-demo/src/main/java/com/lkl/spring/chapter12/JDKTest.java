package com.lkl.spring.chapter12;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class JDKTest {

    interface Foo {
        void foo();

        int bar();
    }

    static class Target implements Foo {
        public void foo() {
            System.out.println("target foo");
        }

        @Override
        public int bar() {
            System.out.println("target bar");
            return 100;
        }
    }

//    interface InvocationHandler {
//        Object invoke(Object proxy, Method method, Object[] args) throws Throwable;
//    }

    public static void main(String[] args) {
        // 生成代理对象
        Foo proxy = new $Proxy0(new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                System.out.println("before...");
                return method.invoke(new Target(), args);
            }

        });

        // java 动态代理 直接生成字节码 【asm技术】
        proxy.foo();
        System.out.println(proxy.bar());
    }

}
