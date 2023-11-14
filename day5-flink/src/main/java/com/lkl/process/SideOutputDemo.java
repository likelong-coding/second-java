package com.lkl.process;

import com.lkl.bean.WaterSensor;
import com.lkl.functions.WaterSensorMapFunction;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

/**
 * @author likelong
 * @date 2023/11/13 23:15
 * @description
 */
public class SideOutputDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        SingleOutputStreamOperator<WaterSensor> sensorDS = env.socketTextStream("124.222.253.33", 7777)
                .map(new WaterSensorMapFunction());
        OutputTag<String> warnTag = new OutputTag<>("warn", Types.STRING);

        // 传感器分组
        SingleOutputStreamOperator<WaterSensor> process = sensorDS.keyBy(WaterSensor::getId)
                .process(
                        new KeyedProcessFunction<String, WaterSensor, WaterSensor>() {
                            @Override
                            public void processElement(WaterSensor value, Context ctx, Collector<WaterSensor> out) throws Exception {
                                // 使用侧输出流告警
                                String currentKey = ctx.getCurrentKey();

                                if (value.getVc() > 10) {
                                    ctx.output(warnTag, "当前传感器=" + currentKey + ",当前水位=" + value.getVc() + ",大于阈值 10！！！");
                                }
                                // 主流正常 发送数据
                                out.collect(value);
                            }
                        }
                );
        process.print("主流");
        process.getSideOutput(warnTag).printToErr("warn");
        env.execute();
    }
}
