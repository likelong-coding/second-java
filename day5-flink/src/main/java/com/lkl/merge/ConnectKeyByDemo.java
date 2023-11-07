package com.lkl.merge;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.datastream.ConnectedStreams;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.CoProcessFunction;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author likelong
 * @date 2023/11/6 21:26
 * @description
 */
public class ConnectKeyByDemo {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
//        env.setParallelism(2);
        DataStreamSource<Tuple2<Integer, String>> source1 = env.fromElements(
                Tuple2.of(1, "a1"),
                Tuple2.of(1, "a2"),
                Tuple2.of(2, "b"),
                Tuple2.of(3, "c")
        );
        DataStreamSource<Tuple3<Integer, String, Integer>> source2 = env.fromElements(
                Tuple3.of(1, "aa1", 1),
                Tuple3.of(1, "aa2", 2),
                Tuple3.of(2, "bb", 1),
                Tuple3.of(3, "cc", 1)
        );

        // 定义HashMap ， 缓存来过的数据，key=id，value=list<数据>
        Map<Integer, List<Tuple2<Integer, String>>> s1Cache = new HashMap<>();
        Map<Integer, List<Tuple3<Integer, String, Integer>>> s2Cache = new HashMap<>();
        ConnectedStreams<Tuple2<Integer, String>, Tuple3<Integer, String, Integer>> connect = source1.connect(source2);
        // 将合流元素，按key分到同一分区才能得到如下结果 ***
        ConnectedStreams<Tuple2<Integer, String>, Tuple3<Integer, String, Integer>> connectKey = connect.keyBy(v -> v.f0, v1 -> v1.f0);
        connectKey.process(
                        new CoProcessFunction<Tuple2<Integer, String>, Tuple3<Integer, String, Integer>, String>() {
                            @Override
                            public void processElement1(Tuple2<Integer, String> value, CoProcessFunction<Tuple2<Integer, String>, Tuple3<Integer, String, Integer>, String>.Context ctx, Collector<String> out) throws Exception {
                                Integer id = value.f0;

                                if (!s1Cache.containsKey(id)) {
                                    List<Tuple2<Integer, String>> s1Values = new ArrayList<>();
                                    s1Values.add(value);
                                    s1Cache.put(id, s1Values);
                                } else {
                                    s1Cache.get(id).add(value);
                                }

                                if (s2Cache.containsKey(id)) {
                                    for (Tuple3<Integer, String, Integer> s2Element : s2Cache.get(id)) {
                                        out.collect("s1:" + value + "<--------->s2:" + s2Element);
                                    }
                                }

                            }

                            @Override
                            public void processElement2
                                    (Tuple3<Integer, String, Integer> value, CoProcessFunction<Tuple2<Integer, String>, Tuple3<Integer, String, Integer>, String>.
                                            Context ctx, Collector<String> out) throws Exception {
                                Integer id = value.f0;
                                if (!s2Cache.containsKey(id)) {
                                    List<Tuple3<Integer, String, Integer>> s2Values = new ArrayList<>();
                                    s2Values.add(value);
                                    s2Cache.put(id, s2Values);
                                } else {
                                    s2Cache.get(id).add(value);
                                }

                                if (s1Cache.containsKey(id)) {
                                    for (Tuple2<Integer, String> s1Element : s1Cache.get(id)) {
                                        out.collect("s1:" + s1Element + "<--------->s2:" + value);
                                    }
                                }

                            }
                        }
                ).

                print();

        env.execute();
    }
}
