package com.lkl.process;

import com.lkl.bean.WaterSensor;
import com.lkl.functions.WaterSensorMapFunction;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.TimerService;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.time.Duration;

/**
 * @author likelong
 * @date 2023/11/13 19:59
 * @description
 */
public class KeyedProcessTimerDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        SingleOutputStreamOperator<WaterSensor> sensorDS = env.socketTextStream("124.222.253.33", 7777)
                .map(new WaterSensorMapFunction())
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy
                                .<WaterSensor>forBoundedOutOfOrderness(Duration.ofSeconds(3))
                                .withTimestampAssigner((element, ts) -> element.getTs() * 1000L)
                );

        // 传感器Id keyBy
        KeyedStream<WaterSensor, String> sensorKS = sensorDS.keyBy(WaterSensor::getId);

        sensorKS.process(new KeyedProcessFunction<String, WaterSensor, String>() {

            /**
             * 来一条数据调用一次
             */
            @Override
            public void processElement(WaterSensor value, KeyedProcessFunction<String, WaterSensor, String>.Context ctx, Collector<String> out) throws Exception {

                // 获取当前数据的 key
                String currentKey = ctx.getCurrentKey();
                // TODO 1.定时器注册
                TimerService timerService = ctx.timerService();
                // 1、事件时间的案例
                Long currentEventTime = ctx.timestamp();//数据中提取出来的事件时间
                timerService.registerEventTimeTimer(5000L);
                System.out.println(" 当前key=" + currentKey + ",当前时间=" + currentEventTime + ",注册了一个5s 的定时器");
                // 2、处理时间的案例
                // long currentTs = timerService.currentProcessingTime();
                // timerService.registerProcessingTimeTimer(currentTs + 5000L);
                // System.out.println(" 当前key="+currentKey + ",当前时间=" + currentTs + ",注册了一个5s 后的定时器");
                // 3、获取 process 的 当前watermark
                // long currentWatermark = timerService.currentWatermark();
                // System.out.println("当前数据=" +value+",当前 watermark=" + currentWatermark);
                // 注册定时器： 处理时间、事件时间
                // timerService.registerProcessingTimeTimer();
                // timerService.registerEventTimeTimer();
                // 删除定时器： 处理时间、事件时间
                // timerService.deleteEventTimeTimer();
                // timerService.deleteProcessingTimeTimer();
                // 获取当前时间进展： 处理时间-当前系统时间，事件时间-当前 watermark
                // long currentTs = timerService.currentProcessingTime();
            }

            /**
             * .时间进展到定时器注册的时间，调用该方法
             * @param timestamp 当前时间进展，就是定时器被触发时的时间
             */
            @Override
            public void onTimer(long timestamp, KeyedProcessFunction<String, WaterSensor, String>.OnTimerContext ctx, Collector<String> out) throws Exception {
                super.onTimer(timestamp, ctx, out);

                String currentKey = ctx.getCurrentKey();
                System.out.println("key=" + currentKey + "现在时间是" + timestamp + "定时器触发");
            }
        }).print();

        env.execute();
    }
}
