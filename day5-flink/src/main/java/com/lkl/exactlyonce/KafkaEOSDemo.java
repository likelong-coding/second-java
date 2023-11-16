package com.lkl.exactlyonce;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.time.Duration;

/**
 * @author likelong
 * @date 2023/11/15 23:40
 * @description 端到端精准一次
 */
public class KafkaEOSDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 【1】、启用检查点,设置为精准一次
        env.enableCheckpointing(5000, CheckpointingMode.EXACTLY_ONCE);
        CheckpointConfig checkpointConfig = env.getCheckpointConfig();
        checkpointConfig.setCheckpointStorage("hdfs://hadoop102:8020/chk");
        checkpointConfig.setExternalizedCheckpointCleanup(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);

        // 2.读取 kafka
        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers("hadoop102:9092")
                .setGroupId("default")
                .setTopics("topic_1")
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .setStartingOffsets(OffsetsInitializer.latest())
                .build();
        DataStreamSource<String> kafkasource = env
                .fromSource(kafkaSource,
                        WatermarkStrategy.forBoundedOutOfOrderness(Duration.ofSeconds(3)), "kafkasource");

        /*
         3.写出到 Kafka
          精准一次 写入 Kafka，需要满足以下条件，【缺一不可】
          1、开启 checkpoint
          2、sink 设置保证级别为 精准一次
          3、sink 设置事务前缀
          4、sink 设置事务超时时间： checkpoint 间隔 < 事务超时时间 < max的15分钟
         */
        KafkaSink<String> kafkaSink = KafkaSink.<String>builder()// 指定 kafka 的地址和端口
                .setBootstrapServers("hadoop102:9092")
                // 指定序列化器：指定 Topic 名称、具体的序列化
                .setRecordSerializer(
                        KafkaRecordSerializationSchema.<String>builder()
                                .setTopic("ws")
                                .setValueSerializationSchema(new SimpleStringSchema())
                                .build()
                )
                // 【3.1】 精准一次,开启 2pc
                .setDeliveryGuarantee(DeliveryGuarantee.EXACTLY_ONCE)

                // 【3.2】 精准一次，必须设置 事务的前缀
                .setTransactionalIdPrefix("li-")
                // 【3.3】 设置事务超时时间
                .setProperty(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, 10 * 60 * 1000 + "")
                .build();
        kafkasource.sinkTo(kafkaSink);
        env.execute();
    }
}
