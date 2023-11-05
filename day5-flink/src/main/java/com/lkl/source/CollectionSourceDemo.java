package com.lkl.source;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author likelong
 * @date 2023/11/4 23:27
 * @description
 */
public class CollectionSourceDemo {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env
                .fromElements(1, 2, 3, 4, 5)  // 直接填写元素
//                .fromCollection(Arrays.asList(1, 2, 3, 4, 5))  // 从集合读取元素
                .print();

        env.execute();
    }
}
