package com.lkl.chapter8;

import java.util.Map;
import java.util.concurrent.*;

/**
 * @author: likelong
 * @date: 2023/8/24 23:15
 * @description:
 */
public class BankWaterService implements Runnable {
    /**
     * 创建4个屏障，处理完之后执行当前类的run方法
     */
    private CyclicBarrier c = new CyclicBarrier(4, this);
    /**
     * 假设只有4个sheet，所以只启动4个线程
     */
    private final Executor executor = Executors.newFixedThreadPool(4);
    /**
     * 保存每个sheet计算出的银流结果
     */
    private final ConcurrentHashMap<String, Integer> sheetBankWaterCount = new
            ConcurrentHashMap<>();

    private void count() {
        for (int i = 0; i < 4; i++) {
            executor.execute(() -> {
                // 计算当前sheet的银流数据，计算代码省略
                sheetBankWaterCount
                        .put(Thread.currentThread().getName(), 1);
                // 银流计算完成，插入一个屏障
                try {
                    c.await();
                } catch (InterruptedException |
                         BrokenBarrierException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @Override
    public void run() {
        int result = 0;
        // 汇总每个sheet计算出的结果
        for (Map.Entry<String, Integer> sheet : sheetBankWaterCount.entrySet()) {
            result += sheet.getValue();
        }
        // 将结果输出
        sheetBankWaterCount.put("result", result);
        System.out.println(result);
    }

    public static void main(String[] args) {
        BankWaterService bankWaterCount = new BankWaterService();
        bankWaterCount.count();
    }
}
