package com.hmdp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author likelong
 */
@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
// SpringBoot项目中无需添加该注解开启事务，已经默认开启
//@EnableTransactionManagement
public class HmDianPingApplication {

    public static void main(String[] args) {
        SpringApplication.run(HmDianPingApplication.class, args);
    }

}
