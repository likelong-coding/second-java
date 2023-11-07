package com.lkl.transform;

import com.lkl.bean.WaterSensor;
import com.lkl.functions.FilterIdFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author likelong
 * @date 2023/11/5 16:54
 * @description
 */
public class TransFilter {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStreamSource<WaterSensor> stream = env.fromElements(
                new WaterSensor("sensor_1", 1L, 1),
                new WaterSensor("sensor_2", 2L, 2)
        );

        stream.filter(new FilterIdFunction("sensor_1"))
                .print();

        env.execute();
    }
}
