package com.lkl.caseDemo;

public class TestBlockingThread {

    static Thread t1 = new Thread(() -> {
        while (true) {
            long start = System.currentTimeMillis();
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long cost = System.currentTimeMillis() - start;
            // 按照正常情况,t1线程,大致上应是每隔1000毫秒左右,会输出一句话 我们使用 cost 来记录实际等待的时间
            // 如果实际时间cost大于1010毫秒 我们就使用System.err输出,也就是红色字样的输出,否则则是正常输出
            (cost > 1010L ? System.err : System.out).printf("thread: %s, costs %d ms\n", Thread.currentThread().getName(), cost);
        }
    });

    static Thread t2 = new Thread(() -> {
        while (true) {

            // 下面是一个counted loop,单次循环末尾不会被加入安全点,整个for循环期执行结束之前,都不会进入安全点
            // 存在这样一种情况, 如果某次for循环才刚刚开始没多久, 因为内存过多而需要进行垃圾收集
            // 【垃圾收集刚开始的时候需要先获取所有根节点,而根节点的获取依赖所有线程抵达安全点】
            // 线程t1很简单,只需要隔1s就会进入安全点,之后,线程t1需要等到其他线程(t2)也进入到安全点
            // 而t2此时才刚刚是for循环的刚开始,所以需要消耗大量时间走完剩下的循环次数,这也就是为什么有时候t1实际cost时间多达5s的原因
            // 也就是gc发生时,要获取所有根节点,而想要获取根节点,就要所有线程抵达安全点,已经抵达的线程(t1)需要等待未抵达的线程(t2)到达安全点
            // 然后才会继续垃圾收集的剩下内容
            for (int i = 1; i <= 1000000000; i++) {
                boolean b = 1.0 / i == 0;
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    });

    private static final int _50KB = 50 * 1024;

    // 下面的代码在创建大量的对象, 一定会导致隔一段时间会出现垃圾收集
    static Thread t3 = new Thread(() -> {
        while (true) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            byte[] bytes = new byte[_50KB];
        }
    });

    public static void main(String[] args) throws InterruptedException {
        t1.start();
        Thread.sleep(1500L);
        t2.start();
        t3.start();
    }
}