package com.lkl.chapter4;

import com.lkl.utils.SleepUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 不建议使用过期标注方法，可能会导致安全隐患
 */
public class Deprecated {
    public static void main(String[] args) throws Exception {
        DateFormat format = new SimpleDateFormat("HH:mm:ss");
        System.out.println("start..." + format.format(new Date()));
        Thread printThread = new Thread(new Runner(), "PrintThread");
        printThread.setDaemon(true);
        printThread.start();
        SleepUtils.second(3);
        // 将PrintThread进行暂停，输出内容工作停止【暂停】
        printThread.suspend();
        System.out.println("main suspend PrintThread at " + format.format(new Date()));
        SleepUtils.second(3);
        // 将PrintThread进行恢复，输出内容继续 【恢复】
        printThread.resume();
        System.out.println("main resume PrintThread at " + format.format(new Date()));
        SleepUtils.second(3);
        // 将PrintThread进行终止，输出内容停止 【停止】
        printThread.stop();
        System.out.println("main stop PrintThread at " + format.format(new Date()));
        SleepUtils.second(3);
        System.out.println("end..." + format.format(new Date()));
    }

    static class Runner implements Runnable {
        @Override
        public void run() {
            DateFormat format = new SimpleDateFormat("HH:mm:ss");
            while (true) {
                System.out.println(Thread.currentThread().getName() + " Run at " +
                        format.format(new Date()));
                SleepUtils.second(1);
            }
        }
    }
}