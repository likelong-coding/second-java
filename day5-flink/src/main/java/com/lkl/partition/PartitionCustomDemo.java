package com.lkl.partition;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class PartitionCustomDemo {
    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(2);
        DataStreamSource<String> socketDS = env.socketTextStream("124.222.253.33", 7777);
        DataStream<String> myDS = socketDS
                .partitionCustom(
                        new MyPartitioner(),
                        value -> value);
        myDS.print();
        env.execute();
    }
}