package com.lkl.partition;

import org.apache.flink.api.common.functions.Partitioner;

public class MyPartitioner implements Partitioner<String> {

    // numPartitions: 子任务数量（分区数量）
    @Override
    public int partition(String key, int numPartitions) {
        return Integer.parseInt(key) % numPartitions;
    }
}