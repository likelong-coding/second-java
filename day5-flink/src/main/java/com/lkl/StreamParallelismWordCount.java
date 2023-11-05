package com.lkl;

import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

/**
 * @author likelong
 * @date 2023/10/31 19:40
 * @description
 */
public class StreamParallelismWordCount {
    public static void main(String[] args) throws Exception {
        // 1. 创建流式执行环境
//        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // idea运行时可以看见web ui，便于本地调试
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(new Configuration());
        env.setParallelism(2);

        env.setRuntimeMode(RuntimeExecutionMode.AUTOMATIC);

        // 2.读取文件
        DataStreamSource<String> lineStream = env.socketTextStream("124.222.253.33", 7777);

        // 3. 转换、分组、求和，得到统计结果
        SingleOutputStreamOperator<Tuple2<String, Long>> sum = lineStream.flatMap(new FlatMapFunction<String, Tuple2<String, Long>>() {
                    @Override
                    public void flatMap(String line, Collector<Tuple2<String, Long>> out) throws Exception {
                        String[] words = line.split(" ");
                        for (String word : words) {
                            out.collect(Tuple2.of(word, 1L));
                        }
                    }
                }).setParallelism(2)
                .keyBy(data -> data.f0)
                .sum(1);

        // 4. 打印
        sum.print();
        // 5. 执行
        env.execute();
    }
}
