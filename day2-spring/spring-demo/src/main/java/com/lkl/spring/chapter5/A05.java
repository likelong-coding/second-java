package com.lkl.spring.chapter5;


import com.lkl.spring.chapter5.mapper.Mapper1;
import com.lkl.spring.chapter5.mapper.Mapper2;
import com.lkl.spring.chapter5.postProcessor.AtBeanPostProcessor;
import com.lkl.spring.chapter5.postProcessor.ComponentScanPostProcessor;
import com.lkl.spring.chapter5.postProcessor.MapperPostProcessor;
import org.springframework.context.support.GenericApplicationContext;

import java.io.IOException;

/*
    BeanFactory 后处理器的作用
 */
public class A05 {

    public static void main(String[] args) throws IOException {

        // ⬇️GenericApplicationContext 是一个【干净】的容器
        GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean("config", Config.class);
//        context.registerBean(ConfigurationClassPostProcessor.class); // @ComponentScan @Bean @Import @ImportResource
//        context.registerBean(MapperScannerConfigurer.class, bd -> { // @MapperScan
//            bd.getPropertyValues().add("basePackage", "com.lkl.spring.chapter5.mapper");
//        });

        context.registerBean(ComponentScanPostProcessor.class); // 解析 @ComponentScan

        context.registerBean(AtBeanPostProcessor.class); // 解析 @Bean
        context.registerBean(MapperPostProcessor.class); // 解析 Mapper 接口

        // ⬇️初始化容器
        context.refresh();

        for (String name : context.getBeanDefinitionNames()) {
            System.out.println(name);
        }

        Mapper1 mapper1 = context.getBean(Mapper1.class);
        Mapper2 mapper2 = context.getBean(Mapper2.class);

        System.out.println("-->" + mapper1);
        System.out.println("-->" + mapper2);

        // ⬇️销毁容器
        context.close();

        /*
            学到了什么
                a. @ComponentScan, @Bean, @Mapper 等注解的解析属于核心容器(即 BeanFactory)的扩展功能
                b. 这些扩展功能由不同的 BeanFactory 后处理器来完成, 其实主要就是补充了一些 bean 定义
         */
    }
}
