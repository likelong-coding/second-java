package com.lkl.functions;

import com.lkl.bean.WaterSensor;
import org.apache.flink.api.common.functions.MapFunction;

/**
 * @author likelong
 * @date 2023/11/6 8:57
 * @description
 */
public class WaterSensorMapFunction implements MapFunction<String, WaterSensor> {
    @Override
    public WaterSensor map(String value) throws Exception {
        String[] split = value.split(",");
        return new WaterSensor(split[0], Long.valueOf(split[1]), Integer.valueOf(split[2]));
    }
}
