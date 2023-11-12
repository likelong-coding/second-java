package com.lkl.watermark;

import com.lkl.bean.WaterSensor;
import com.lkl.functions.WaterSensorMapFunction;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author likelong
 * @date 2023/11/11 22:36
 * @description
 */
public class CustomWaterMarkDemo {
    public static void main(String[] args) {
        // 1. 创建流式执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.getConfig().setAutoWatermarkInterval(400L);

        // 2.读取文件
        DataStreamSource<String> lineStream = env.socketTextStream("124.222.253.33", 7777);

        SingleOutputStreamOperator<WaterSensor> stream = lineStream.map(new WaterSensorMapFunction());

        WatermarkStrategy<WaterSensor> watermarkStrategy = WatermarkStrategy
                .<WaterSensor>forMonotonousTimestamps()
                // 指定时间戳分配器，从数据中提取 单位毫秒
                .withTimestampAssigner((SerializableTimestampAssigner<WaterSensor>) (element, recordTimestamp) -> {
                    System.out.println(" 数据 =" + element + ",recordTs=" + recordTimestamp);
                    return element.getTs() * 1000L;
                });

        stream.assignTimestampsAndWatermarks(watermarkStrategy);
    }
}
