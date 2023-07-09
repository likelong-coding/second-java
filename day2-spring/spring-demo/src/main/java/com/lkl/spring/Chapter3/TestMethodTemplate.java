package com.lkl.spring.Chapter3;

import java.util.ArrayList;
import java.util.List;

public class TestMethodTemplate {

    public static void main(String[] args) {
        MyBeanFactory beanFactory = new MyBeanFactory();
        beanFactory.addBeanPostProcessor(bean -> System.out.println("解析 @Autowired"));
        beanFactory.addBeanPostProcessor(bean -> System.out.println("解析 @Resource"));
        beanFactory.getBean();
    }

    // 模板方法  Template Method Pattern
    static class MyBeanFactory {
        private final List<BeanPostProcessor> processors = new ArrayList<>();

        /**
         * 构造、依赖注入、初始化以及销毁这些方法都是固定的保持不动
         * 在依赖注入阶段扩展属于会变化的部分，抽象成接口动态添加增强方法
         */
        public Object getBean() {
            Object bean = new Object();
            System.out.println("构造 " + bean);
            System.out.println("依赖注入 " + bean); // @Autowired, @Resource
            // 会变化的部分
            for (BeanPostProcessor processor : processors) {
                processor.inject(bean);
            }
            System.out.println("初始化 " + bean);
            System.out.println("销毁" + bean);
            return bean;
        }

        public void addBeanPostProcessor(BeanPostProcessor processor) {
            processors.add(processor);
        }
    }

    /**
     * 抽象接口
     */
    interface BeanPostProcessor {
        // 对依赖注入阶段的扩展
        void inject(Object bean);
    }
}