package com.lkl.topN;

import com.lkl.bean.WaterSensor;
import com.lkl.functions.WaterSensorMapFunction;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.time.Duration;
import java.util.*;

/**
 * @author likelong
 * @date 2023/11/13 22:49
 * @description
 */
public class KeyedProcessFunctionTopNDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        SingleOutputStreamOperator<WaterSensor> sensorDS = env.socketTextStream("124.222.253.33", 7777)
                .map(new WaterSensorMapFunction())
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy
                                .<WaterSensor>forBoundedOutOfOrderness(Duration.ofSeconds(3))
                                .withTimestampAssigner((element, ts) -> element.getTs() * 1000L)
                );

        KeyedStream<WaterSensor, Integer> keyedStream = sensorDS.keyBy(WaterSensor::getVc);

        /*
        思路二： 使用 KeyedProcessFunction 实现
        1、按照 vc 做 keyby，开窗，分别 count
            ==》 增量聚合，计算 count
            ==》 全窗口，对计算结果 count 值封装，带上窗口结束时间的标签
            ==》 为了让同一个窗口时间范围的计算结果到一起去
        2、对同一个窗口范围的 count 值进行处理：排序、取前N 个
            =》 按照 windowEnd 做 keyby
            =》 使用 process， 来一条调用一次，需要先存，分开存，用HashMap,key=windowEnd,value=List
            =》 使用定时器，对 存起来的结果 进行排序、取前N个
            */
        // 1. 按照 vc 分组、开窗、聚合（增量计算+全量打标签）
        // 开窗聚合后，就是普通的流，没有了窗口信息，需要自己打上窗口的标记windowEnd
        SingleOutputStreamOperator<Tuple3<Integer, Integer, Long>> windowAgg = keyedStream
                .window(SlidingEventTimeWindows.of(Time.seconds(10), Time.seconds(5)))
                .aggregate(
                        new VcCountAgg(),
                        new WindowResult()
                );
        // 2. 按照窗口标签（窗口结束时间）keyby，保证同一个窗口时间范围的结果，到一起去。排序、取 TopN
        windowAgg.keyBy(r -> r.f2)
                .process(new TopN(2))
                .print();
        env.execute();
    }

    public static class VcCountAgg implements AggregateFunction<WaterSensor, Integer, Integer> {
        @Override
        public Integer createAccumulator() {
            return 0;
        }

        @Override
        public Integer add(WaterSensor value, Integer accumulator) {
            return accumulator + 1;
        }

        @Override
        public Integer getResult(Integer accumulator) {
            return accumulator;
        }

        @Override
        public Integer merge(Integer a, Integer b) {
            return null;
        }
    }

    /**
     * 泛型如下：
     * 第一个：输入类型 = 增量函数的输出 count 值，Integer
     * 第二个：输出类型 = Tuple3(vc，count，windowEnd) ,带上窗口结束时间的标签
     * 第三个：key 类型 ， vc，Integer
     * 第四个：窗口类型
     */
    public static class WindowResult extends ProcessWindowFunction<Integer, Tuple3<Integer, Integer, Long>, Integer, TimeWindow> {
        @Override
        public void process(Integer key, Context context, Iterable<Integer> elements, Collector<Tuple3<Integer, Integer, Long>> out) throws Exception {
            // 迭代器里面只有一条数据，next 一次即可
            Integer count = elements.iterator().next();
            long windowEnd = context.window().getEnd();
            out.collect(Tuple3.of(key, count, windowEnd));
        }
    }

    public static class TopN extends KeyedProcessFunction<Long, Tuple3<Integer, Integer, Long>, String> {
        // 存不同窗口的 统计结果，key=windowEnd，value=list 数据
        private Map<Long, List<Tuple3<Integer, Integer, Long>>> dataListMap;
        // 要取的 Top 数量
        private int threshold;

        public TopN(int threshold) {
            this.threshold = threshold;
            dataListMap = new HashMap<>();
        }

        @Override
        public void processElement(Tuple3<Integer, Integer, Long> value, Context ctx, Collector<String> out) throws Exception {
            // 进入这个方法，只是一条数据，要排序，得到齐才行===》存起来，不同窗口分开存
            // 1. 存到 HashMap 中
            Long windowEnd = value.f2;
            if (dataListMap.containsKey(windowEnd)) {
                // 1.1 包含 vc，不是该 vc 的第一条，直接添加到List中
                List<Tuple3<Integer, Integer, Long>> dataList = dataListMap.get(windowEnd);
                dataList.add(value);
            } else {
                // 1.1 不包含 vc，是该 vc 的第一条，需要初始化list
                List<Tuple3<Integer, Integer, Long>> dataList = new ArrayList<>();
                dataList.add(value);
                dataListMap.put(windowEnd, dataList);
            }
            // 2. 注册一个定时器， windowEnd+1ms 即可 延迟1ms 触发即可，及时性
            ctx.timerService().registerEventTimeTimer(windowEnd + 1);
        }

        @Override
        public void onTimer(long timestamp, OnTimerContext ctx, Collector<String> out) throws Exception {
            super.onTimer(timestamp, ctx, out);
            // 定时器触发，同一个窗口范围的计算结果攒齐了，开始排序、取TopN
            Long windowEnd = ctx.getCurrentKey();
            // 1. 排序
            List<Tuple3<Integer, Integer, Long>> dataList = dataListMap.get(windowEnd);
            dataList.sort(new Comparator<Tuple3<Integer, Integer, Long>>() {
                @Override
                public int compare(Tuple3<Integer, Integer, Long> o1, Tuple3<Integer, Integer, Long> o2) {
                    return o2.f1 - o1.f1;
                }
            });
            // 2. 取 TopN
            StringBuilder outStr = new StringBuilder();
            outStr.append("================================\n");
            for (int i = 0; i < Math.min(threshold, dataList.size()); i++) {
                Tuple3<Integer, Integer, Long> vcCount = dataList.get(i);
                outStr.append("Top").append(i + 1).append("\n");
                outStr.append("vc=").append(vcCount.f0).append("\n");
                outStr.append("count=").append(vcCount.f1).append("\n");
                outStr.append("窗口结束时间=").append(vcCount.f2).append("\n");
                outStr.append("================================\n");
            }
            // 用完的 List，及时清理，节省资源
            dataList.clear();
            out.collect(outStr.toString());
        }
    }
}
