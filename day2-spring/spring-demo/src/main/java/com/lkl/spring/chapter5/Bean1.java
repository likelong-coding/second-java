package com.lkl.spring.chapter5;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bean1 {

    private static final Logger log = LoggerFactory.getLogger(Bean1.class);

    public Bean1() {
        log.debug("我被 Spring 管理啦 bean1");
    }
}
