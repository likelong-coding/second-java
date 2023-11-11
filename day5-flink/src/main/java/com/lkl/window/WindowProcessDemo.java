package com.lkl.window;

import com.lkl.bean.WaterSensor;
import com.lkl.functions.WaterSensorMapFunction;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

/**
 * @author likelong
 * @date 2023/11/9 22:00
 * @description
 */
public class WindowProcessDemo {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        SingleOutputStreamOperator<WaterSensor> sensorDS = env.socketTextStream("124.222.253.33", 7777)
                .map(new WaterSensorMapFunction());
        KeyedStream<WaterSensor, String> sensorKS = sensorDS.keyBy(WaterSensor::getId);
        WindowedStream<WaterSensor, String, TimeWindow> sensorWS = sensorKS.window(TumblingProcessingTimeWindows.of(Time.seconds(10)));
        SingleOutputStreamOperator<String> process = sensorWS.process(
                new ProcessWindowFunction<WaterSensor,
                        String, String, TimeWindow>() {
                    @Override
                    public void process(String s, Context context, Iterable<WaterSensor> elements, Collector<String> out) throws Exception {
                        long count = elements.spliterator().estimateSize();
                        long windowStartTs = context.window().getStart();
                        long windowEndTs = context.window().getEnd();
                        String windowStart = DateFormatUtils.format(windowStartTs, "yyyy-MM-dd HH:mm:ss.SSS");
                        String windowEnd = DateFormatUtils.format(windowEndTs, "yyyy-MM-dd HH:mm:ss.SSS");
                        out.collect("key=" + s + "的窗口[" + windowStart + "," + windowEnd + ") 包含 " + count + " 条数据===>" + elements);
                    }
                }
        );
        process.print();
        env.execute();
    }
}
