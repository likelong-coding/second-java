package com.lkl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;

/**
 * 应该是asm技术生成字节码文件，反编译得到类跟这个差不多
 */
public class $Proxy0 extends Proxy implements Foo {

    protected $Proxy0(InvocationHandler h) {
        super(h);
    }

    @Override
    public void foo() {
        try {
            h.invoke(this, foo, null);
        } catch (Throwable e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    static Method foo;

    static {
        try {
            foo = Foo.class.getMethod("foo");
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
    }
}
