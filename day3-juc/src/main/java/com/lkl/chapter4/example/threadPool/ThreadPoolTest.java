package com.lkl.chapter4.example.threadPool;

public class ThreadPoolTest {
    public static void main(String[] args) {
        ThreadPool<ComputeTask> threadPool = new DefaultThreadPool<>();

        threadPool.execute(new ComputeTask(1, 100));
        threadPool.execute(new ComputeTask(2, 500));
        System.out.println(threadPool.getJobSize());

        threadPool.shutdown();

    }

    static class ComputeTask implements Runnable {
        private final int start;
        private final int end;

        public ComputeTask(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public void run() {
            int sum = 0;
            for (int i = start; i <= end; i++) {
                sum += i;
            }
            System.out.println("sum = " + sum);
        }
    }
}
