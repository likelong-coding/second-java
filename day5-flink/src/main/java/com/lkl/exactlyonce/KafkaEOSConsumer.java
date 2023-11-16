package com.lkl.exactlyonce;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.kafka.clients.consumer.ConsumerConfig;

import java.time.Duration;

/**
 * @author likelong
 * @date 2023/11/15 23:49
 * @description
 */
public class KafkaEOSConsumer {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 消费 在前面使用【两阶段提交】写入的 Topic
        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers("hadoop102:9092")
                .setGroupId("default")
                .setTopics("ws")
                .setValueOnlyDeserializer(new SimpleStringSchema()).setStartingOffsets(OffsetsInitializer.latest())
                // 作为 下游的消费者，要设置事务的隔离级别为 【读已提交】
                .setProperty(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed")
                .build();
        env
                .fromSource(kafkaSource,
                        WatermarkStrategy.forBoundedOutOfOrderness(Duration.ofSeconds(3)), "kafkasource")
                .print();
        env.execute();
    }
}
