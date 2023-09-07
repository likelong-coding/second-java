package com.lkl.chapter9;

import java.util.concurrent.*;

/**
 * @author likelong
 * @date 2023/9/6 23:13
 * @description
 */
public class FutureTaskTest {
    private static final ConcurrentMap<String, Future<String>> taskCache = new ConcurrentHashMap<>();

    private static String executionTask(final String taskName) throws ExecutionException, InterruptedException {
        while (true) {
            Future<String> future = taskCache.get(taskName);
            if (future == null) {
                Callable<String> task = () -> {
                    System.out.println("execute task:" + taskName);
                    Thread.sleep(1000);
                    return taskName;
                };                                                                                                // 1.2创建任务
                FutureTask<String> futureTask = new FutureTask<>(task);
                future = taskCache.putIfAbsent(taskName, futureTask);
                if (future == null) {
                    future = futureTask;
                    futureTask.run();
                }
            }
            try {
                return future.get();
            } catch (CancellationException e) {
                taskCache.remove(taskName, future);
            }
        }
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        for (int i = 0; i < 5; i++) {
            System.out.println("----------start-----------");
            System.out.println(executionTask("task_" + i));
            System.out.println("----------end-----------\n");
        }
    }
}
