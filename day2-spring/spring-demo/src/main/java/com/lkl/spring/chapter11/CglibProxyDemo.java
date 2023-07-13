package com.lkl.spring.chapter11;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;

public class CglibProxyDemo {

    static class Target {
        public void foo() {
            System.out.println("target foo");
        }
    }

    // 代理是子类型, 目标是父类型 目标类加 final 就无法创建子类，直接报错 【cglib 基于子父类关系代理】
    // 增强目标不能是 final 增强方法也不能是 final
    public static void main(String[] param) {
        Target target = new Target();

        Target proxy = (Target) Enhancer.create(Target.class, (MethodInterceptor) (p, method, args, methodProxy) -> {
            System.out.println("before...");
//            Object result = method.invoke(target, args); // 用方法反射调用目标 方法一
            // methodProxy 它可以避免反射调用
//            Object result = methodProxy.invoke(target, args); // 内部没有用反射, 需要目标 （spring）方法二
            Object result = methodProxy.invokeSuper(p, args); // 内部没有用反射, 需要代理 方法三
            System.out.println("after...");
            return result;
        });

        proxy.foo();

    }
}