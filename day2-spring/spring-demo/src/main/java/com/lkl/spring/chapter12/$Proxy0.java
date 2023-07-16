package com.lkl.spring.chapter12;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;

/**
 * JDK动态代理实现相同接口，生成代理类
 */
public class $Proxy0 extends Proxy implements JDKTest.Foo {

//    InvocationHandler h;

    public $Proxy0(InvocationHandler h) {
        super(h);
    }

    @Override
    public void foo() {
        try {
            // 不确定的方法使用接口抽象
            h.invoke(this, foo, new Object[0]);
            // 有异常直接抛出
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    @Override
    public int bar() {
        try {
            Object result = h.invoke(this, bar, new Object[0]);
            return (int) result;
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    static Method foo;
    static Method bar;

    // 静态代码块，只加载一次
    static {
        try {
            foo = JDKTest.Foo.class.getMethod("foo");
            bar = JDKTest.Foo.class.getMethod("bar");
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
    }
}
