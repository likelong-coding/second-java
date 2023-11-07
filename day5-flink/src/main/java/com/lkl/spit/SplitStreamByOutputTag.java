package com.lkl.spit;

import com.lkl.bean.WaterSensor;
import com.lkl.functions.WaterSensorMapFunction;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.streaming.api.datastream.SideOutputDataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

/**
 * @author likelong
 * @date 2023/11/6 8:56
 * @description
 */
public class SplitStreamByOutputTag {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        SingleOutputStreamOperator<WaterSensor> ds = env.socketTextStream("124.222.253.33", 7777)
                .map(new WaterSensorMapFunction());
        OutputTag<WaterSensor> s1 = new OutputTag<WaterSensor>("s1", Types.POJO(WaterSensor.class)) {
        };
        OutputTag<WaterSensor> s2 = new OutputTag<WaterSensor>("s2", Types.POJO(WaterSensor.class)) {
        };

        //返回的都是主流
        SingleOutputStreamOperator<WaterSensor> ds1 = ds.process(new ProcessFunction<WaterSensor, WaterSensor>() {
            @Override
            public void processElement(WaterSensor value, ProcessFunction<WaterSensor, WaterSensor>.Context ctx, Collector<WaterSensor> out) throws Exception {
                if ("s1".equals(value.getId())) {
                    ctx.output(s1, value);
                } else if ("s2".equals(value.getId())) {
                    ctx.output(s2, value);
                } else {
                    //主流
                    out.collect(value);
                }
            }
        });

        // 打印主流
        ds1.print("主流数据");
        // 通过主流获取侧边流
        SideOutputDataStream<WaterSensor> sideOutput1 = ds1.getSideOutput(s1);
        SideOutputDataStream<WaterSensor> sideOutput2 = ds1.getSideOutput(s2);
        sideOutput1.printToErr("测流s1");
        sideOutput2.printToErr("测流s2");

        env.execute();

    }
}
