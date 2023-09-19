package com.lkl;

import java.util.ArrayList;
import java.util.List;

/**
 * @author likelong
 * @date 2023/9/18 23:35
 * @description VM options: -Xms20m -Xmx20m -XX:+HeapDumpOnOutOfMemoryError
 */
public class HeapOOM {
    static class OOMObject {

    }

    public static void main(String[] args) {
        List<OOMObject> list = new ArrayList<>();
        while (true) {
            list.add(new OOMObject());
        }
    }
}