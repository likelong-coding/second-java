package com.lkl.spring.chapter6;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringValueResolver;

@Component   // 注意该类必须在ioc容器中使用，否则EmbeddedValueResolverAware不会注入进来
public class PropertiesUtil implements EmbeddedValueResolverAware {

	private static final Logger log = LoggerFactory.getLogger(PropertiesUtil.class);

	private static StringValueResolver valueResolver;

	@Override
	public void setEmbeddedValueResolver(StringValueResolver resolver) {
		log.info("resolver " + resolver);
		PropertiesUtil.valueResolver = resolver;

		log.info("age " + PropertiesUtil.getValue("person.age"));
		log.info("name " + PropertiesUtil.getValue("person.name"));
	}

	/**
	 * 该方式只适用于被spring接管的properties
	 */
	public static String getValue(String key) {
		// StringValueResolver还可以解析spel表达式，@Value注解能够解析的，StringValueResolver都可以解析
		return valueResolver.resolveStringValue("${" + key + "}");
	}

}