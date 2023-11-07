package com.lkl.aggregation;

import com.lkl.bean.WaterSensor;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author likelong
 * @date 2023/11/5 21:31
 * @description
 */
public class SimpleDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        DataStreamSource<WaterSensor> stream = env.fromElements(
                new WaterSensor("sensor_1", 1L, 1),
                new WaterSensor("sensor_1", 2L, 2),
                new WaterSensor("sensor_2", 2L, 2),
                new WaterSensor("sensor_3", 3L, 3)
        );

        KeyedStream<WaterSensor, String> keyedStream = stream.keyBy(WaterSensor::getId);
        keyedStream
                .maxBy("vc") // 指定字段名称
                .print();
        env.execute();
    }
}
