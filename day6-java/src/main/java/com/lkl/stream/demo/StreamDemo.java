package com.lkl.stream.demo;

import com.lkl.stream.entity.People;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author likelong
 * @date 2023/11/22 13:49
 * @description
 */
public class StreamDemo {
    public static void main(String[] args) {
        List<People> list = new ArrayList<>();
        list.add(new People("wyl", 18));
        list.add(new People("lkl", 23));

        List<Integer> ageList = list.stream()
                .map(People::getAge)
                .filter(x -> x % 2 == 0)
                .collect(Collectors.toList());

    }
}
