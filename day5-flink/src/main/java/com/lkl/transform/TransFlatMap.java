package com.lkl.transform;

import com.lkl.bean.WaterSensor;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

/**
 * @author likelong
 * @date 2023/11/5 16:54
 * @description
 */
public class TransFlatMap {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStreamSource<WaterSensor> stream = env.fromElements(
                new WaterSensor("sensor_1", 1L, 1),
                new WaterSensor("sensor_2", 2L, 2)
        );

        stream.flatMap(
                new FlatMapFunction<WaterSensor, String>() {
                    @Override
                    public void flatMap(WaterSensor value, Collector<String> out) throws Exception {
                        if ("sensor_1".equals(value.getId())) {
                            out.collect(value.getVc().toString());
                        } else if ("sensor_2".equals(value.getId())) {
                            out.collect(value.getTs().toString());
                            out.collect(value.getVc().toString());
                        }
                    }
                }
        ).print();

        env.execute();
    }
}
