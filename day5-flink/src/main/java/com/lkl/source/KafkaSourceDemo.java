package com.lkl.source;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author likelong
 * @date 2023/11/5 11:12
 * @description
 */
public class KafkaSourceDemo {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers("124.222.253.33:9092") // ip:port
                .setTopics("topic_1") // topic
                .setGroupId("group1") // 消费者组
                // latest 将偏移初始化为最新偏移的OffsetInitializer
                // earliest 偏移初始化为最早可用偏移的OffsetInitializer
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new SimpleStringSchema()).build(); // 仅Value反序列化

        env
                .fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "kafka-source")
                .print();
        env.execute();
    }
}
