package com.lkl.topN;

import com.lkl.bean.WaterSensor;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.windowing.ProcessAllWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.util.*;

/**
 * @author likelong
 * @date 2023/11/13 22:26
 * @description
 */
public class MyTopNPAWF extends ProcessAllWindowFunction<WaterSensor, String, TimeWindow> {

    @Override
    public void process(ProcessAllWindowFunction<WaterSensor, String, TimeWindow>.Context context, Iterable<WaterSensor> elements, Collector<String> out) throws Exception {
        Map<Integer, Integer> vcCountMap = new HashMap<>();

        for (WaterSensor element : elements) {
            // 统计不同水位出现次数
            vcCountMap.put(element.getVc(), vcCountMap.getOrDefault(element.getVc(), 0) + 1);
        }

        // 对 count 值进行排序: 利用 List 来实现排序
        List<Tuple2<Integer, Integer>> datas = new ArrayList<>();
        for (Integer vc : vcCountMap.keySet()) {
            datas.add(Tuple2.of(vc, vcCountMap.get(vc)));
        }
        // 对 List 进行排序，根据 count 值 降序
        datas.sort(new Comparator<Tuple2<Integer, Integer>>() {
            @Override
            public int compare(Tuple2<Integer, Integer> o1, Tuple2<Integer, Integer> o2) {
                // 降序， 后 减 前
                return o2.f1 - o1.f1;
            }
        });

        StringBuilder outStr = new StringBuilder();
        outStr.append("================================\n");
        // 遍历 排序后的 List，取出前 2 个， 考虑可能List 不够2个的情况==》 List 中元素的个数 和 2 取最小值
        for (int i = 0; i < Math.min(2, datas.size()); i++) {
            Tuple2<Integer, Integer> vcCount = datas.get(i);
            outStr.append("Top").append(i + 1).append("\n");
            outStr.append("vc=").append(vcCount.f0).append("\n");
            outStr.append("count=").append(vcCount.f1).append("\n");
            outStr.append(" 窗 口 结束时间=").append(DateFormatUtils.format(context.window().getEnd(), "yyyy-MM-ddHH:mm:ss.SSS")).append("\n");
            outStr.append("================================\n");
        }
        out.collect(outStr.toString());
    }
}
