package com.lkl.functions;

import com.lkl.bean.WaterSensor;
import org.apache.flink.api.common.functions.FilterFunction;

/**
 * @author likelong
 * @date 2023/11/5 23:36
 * @description
 */
public class FilterIdFunction implements FilterFunction<WaterSensor> {

    private final String id;

    public FilterIdFunction(String id) {
        this.id = id;
    }

    @Override
    public boolean filter(WaterSensor value) throws Exception {
        return id.equals(value.getId());
    }
}
