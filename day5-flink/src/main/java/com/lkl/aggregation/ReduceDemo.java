package com.lkl.aggregation;

import com.lkl.bean.WaterSensor;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author likelong
 * @date 2023/11/5 21:40
 * @description
 */
public class ReduceDemo {

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
                .reduce(new ReduceFunction<WaterSensor>() {
                    // 同组元素规约处理
                    @Override
                    public WaterSensor reduce(WaterSensor value1, WaterSensor value2) throws Exception {

                        System.out.println("value1: " + value1);
                        System.out.println("value2: " + value2);
                        int maxVc = Math.max(value1.getVc(), value2.getVc());
                        if (value1.getVc() > value2.getVc()) {
                            value1.setVc(maxVc);
                            return value1;
                        } else {
                            value2.setVc(maxVc);
                            return value2;
                        }
                    }
                })
                .print();
        env.execute();
    }
}
