package com.lkl.sink;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.kafka.clients.producer.ProducerConfig;

/**
 * @author likelong
 * @date 2023/11/6 23:32
 * @description
 */
public class SinkKafka {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        // 如果是精准一次，必须开启 checkpoint
        env.enableCheckpointing(2000, CheckpointingMode.EXACTLY_ONCE);

        SingleOutputStreamOperator<String> sensorDS = env.socketTextStream("124.222.253.33", 7777);

        /*
          Kafka Sink:
           注意：如果要使用 【精准一次】 写入 Kafka，需要满足以下条件，缺一不可
          1、开启 checkpoint
          2、设置事务前缀
          3、设置事务超时时间： checkpoint 间隔 < 事务超时时间 < max的 15分钟
         */
        KafkaSink<String> kafkaSink = KafkaSink.<String>builder()// 指定 kafka 的地址和端口
                .setBootstrapServers("124.222.253.33:9092")
                // 指定序列化器：指定 Topic 名称、具体的序列化
                .setRecordSerializer(
                        KafkaRecordSerializationSchema.<String>builder()
                                .setTopic("ws")
                                .setValueSerializationSchema(new SimpleStringSchema())
                                .build()
                )
                // 写到 kafka 的一致性级别： 精准一次、至少一次
                .setDeliveryGuarantee(DeliveryGuarantee.EXACTLY_ONCE)
                // 如果是精准一次，必须设置 事务的前缀
                .setTransactionalIdPrefix("li-")
                // 如果是精准一次，必须设置 事务超时时间: 大于checkpoint间隔，小于 max 15 分钟
                .setProperty(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, 10 * 60 * 1000 + "")
                .build();
        sensorDS.sinkTo(kafkaSink);
        env.execute();

    }
}
