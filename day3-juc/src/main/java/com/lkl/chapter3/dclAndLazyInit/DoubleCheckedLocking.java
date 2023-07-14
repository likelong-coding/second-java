package com.lkl.chapter3.dclAndLazyInit;

import com.lkl.entity.Singleton;

/**
 * DCL 双重检测锁 单例模式
 */
public class DoubleCheckedLocking {

    // volatile 保证创建对象时不会发生重排序
    private volatile static Singleton instance;

    public static Singleton getInstance() {

        // 第一次检查 （代码读取到instance不为null时，instance引用的对象有可能还没有完成初始化。）
        if (instance == null) {
            // synchronized锁更少的代码性能会好一些
            synchronized (DoubleCheckedLocking.class) {  // 加锁
                if (instance == null)                    // 第二次检查
                    /*
                        问题的根源出在这里，对象不加 volatile 修饰，创建对象时可能发生重排序，导致先创建一个空对象，后续在初始化，
                        单线程最后结果不会受到影响所以可以重排序，此时另一个线程第一次检查不为null，直接返回结果但是此时对象还未初始化
                        解决问题只需将 对象由 volatile修饰禁止指令重排即可
                     */
                    instance = new Singleton();

                    /*
                        创建对象三部曲（正确顺序）：
                        1、分配对象的内存空间
                        2、初始化对象
                        3、设置 instance 指向刚分配的内存地址

                        步骤2、3可能重排序
                     */

            }
        }
        return instance;
    }
}