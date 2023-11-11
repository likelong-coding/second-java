package com.lkl.window;

import com.lkl.bean.WaterSensor;
import com.lkl.functions.WaterSensorMapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

/**
 * @author likelong
 * @date 2023/11/9 8:22
 * @description
 */
public class WindowReduceDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        DataStreamSource<String> stream = env.socketTextStream("124.222.253.33", 7777);

        stream.map(new WaterSensorMapFunction())
                .keyBy(WaterSensor::getId)
                // 设置滚动事件时间窗口
                .window(TumblingProcessingTimeWindows.of(Time.seconds(10)))
                .reduce(new ReduceFunction<WaterSensor>() {
                    @Override
                    public WaterSensor reduce(WaterSensor value1, WaterSensor value2) throws Exception {
                        System.out.println("调用reduce 方法，之前的结果:" + value1 + ",现在来的数据:" + value2);
                        return new WaterSensor(value1.getId(), System.currentTimeMillis(), value1.getVc() + value2.getVc());
                    }
                })
                .print();
        env.execute();
    }
}
