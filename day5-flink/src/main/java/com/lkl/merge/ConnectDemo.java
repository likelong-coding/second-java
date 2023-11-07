package com.lkl.merge;

import org.apache.flink.streaming.api.datastream.ConnectedStreams;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.CoMapFunction;

/**
 * @author likelong
 * @date 2023/11/6 10:06
 * @description
 */
public class ConnectDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        DataStreamSource<Integer> source1 = env.fromElements(1, 2, 3);

        DataStreamSource<String> source2 = env.fromElements("a", "b", "c");
        ConnectedStreams<Integer, String> connect = source1.connect(source2);
        /*
         connect:
          1、一次只能连接 2 条流
          2、流的数据类型可以不一样
          3、连接后可以调用 map、flatmap、process 来处理，但是各处理各的
         */
        connect.map(new CoMapFunction<Integer, String, String>() {
                    @Override
                    public String map1(Integer value) throws Exception {
                        return "来源于数字流:" + value.toString();
                    }

                    @Override
                    public String map2(String value) throws Exception {
                        return "来源于字母流:" + value;
                    }
                })
                .print();

        env.execute();
    }
}
