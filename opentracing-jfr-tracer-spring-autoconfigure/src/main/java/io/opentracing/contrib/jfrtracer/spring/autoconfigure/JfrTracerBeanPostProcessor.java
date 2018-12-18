package io.opentracing.contrib.jfrtracer.spring.autoconfigure;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import io.opentracing.Tracer;
import io.opentracing.contrib.jfrtracer.JfrTracerFactory;

@Configuration
@ConditionalOnProperty(value = "opentracing.jfr-tracer.enabled", havingValue = "true", matchIfMissing = true)
public class JfrTracerBeanPostProcessor implements BeanPostProcessor {

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof Tracer) {
			return JfrTracerFactory.create((Tracer) bean);
		}
		return bean;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

}
