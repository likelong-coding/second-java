package com.lkl.sink;

import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.nio.charset.StandardCharsets;

/**
 * @author likelong
 * @date 2023/11/6 23:40
 * @description
 */
public class SinkKafkaWithKey {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.enableCheckpointing(2000, CheckpointingMode.EXACTLY_ONCE);
        env.setRestartStrategy(RestartStrategies.noRestart());
        SingleOutputStreamOperator<String> sensorDS = env.socketTextStream("124.222.253.33", 7777);

        /*
         如果要指定写入 kafka 的 key，可以自定义序列化器：
          1、实现 一个接口，重写 序列化 方法
          2、指定 key，转成 字节数组
          3、指定 value，转成 字节数组
          4、返回一个 ProducerRecord 对象，把 key、value 放进去
         */
        KafkaSink<String> kafkaSink = KafkaSink.<String>builder().setBootstrapServers("124.222.253.33:9092")
                .setRecordSerializer(
                        new KafkaRecordSerializationSchema<String>() {
                            @Override
                            public ProducerRecord<byte[], byte[]> serialize(String element, KafkaSinkContext context, Long timestamp) {
                                String[] datas = element.split(",");
                                byte[] key = datas[0].getBytes(StandardCharsets.UTF_8);
                                byte[] value = element.getBytes(StandardCharsets.UTF_8);
                                return new ProducerRecord<>("ws", key, value);
                            }
                        }
                )
                .setDeliveryGuarantee(DeliveryGuarantee.EXACTLY_ONCE)
                .setTransactionalIdPrefix("li-")
                .setProperty(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, 10 * 60 * 1000 + "")
                .build();
        sensorDS.sinkTo(kafkaSink);
        env.execute();
    }
}


