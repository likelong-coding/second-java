package com.lkl.source;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.connector.source.util.ratelimit.RateLimiterStrategy;
import org.apache.flink.connector.datagen.source.DataGeneratorSource;
import org.apache.flink.connector.datagen.source.GeneratorFunction;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author likelong
 * @date 2023/11/5 12:57
 * @description
 */
public class DataGenSourceDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);

        DataGeneratorSource<String> dataGeneratorSource = new DataGeneratorSource<>(
                // 数据转换生成函数
                new GeneratorFunction<Long, String>() {
                    @Override
                    public String map(Long value) throws Exception {
                        return "Number:" + value;
                    }
                },
                // 从0开始递增至这个数
                Long.MAX_VALUE,
                // 数据生产效率，每秒10个
                RateLimiterStrategy.perSecond(10), Types.STRING

        );
        env
                .fromSource(dataGeneratorSource,
                        WatermarkStrategy.noWatermarks(), "dataGenerator")
                .print();

        env.execute();
    }
}
