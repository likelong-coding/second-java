package com.hmdp.annotation;

import java.lang.annotation.*;

/**
 * 统计一个方法耗时注解
 * @author likelong
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TimeConsuming{
}
