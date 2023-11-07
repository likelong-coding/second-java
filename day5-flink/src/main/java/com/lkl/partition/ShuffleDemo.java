package com.lkl.partition;

import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author likelong
 * @date 2023/11/5 23:52
 * @description
 */
public class ShuffleDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(2);

        DataStreamSource<String> source = env.socketTextStream("124.222.253.33", 7777);

        source.shuffle().print();

        env.execute();
    }
}
