package io.opentracing.contrib.jfrtracer.spring.autoconfigure;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import io.opentracing.Tracer;
import io.opentracing.contrib.jfrtracer.JfrTracerFactory;

/**
 * BeanPostProcessor {@link BeanPostProcessor} wraps available {@link Tracer} into jfr
 * {@link io.opentracing.contrib.jfrtracer.impl.wrapper.TracerWrapper}. Configuration is enabled by
 * default and can be disabled by setting up environment variable opentracing.jfr-tracer.enabled to
 * false
 */
@Configuration
@ConditionalOnProperty(value = "opentracing.jfr-tracer.enabled", havingValue = "true", matchIfMissing = true)
public class JfrTracerBeanPostProcessor implements BeanPostProcessor {

	/**
	 * wrapping new instantiated tracer bean to JFR Tracer see
	 * {@link BeanPostProcessor#postProcessAfterInitialization(Object, String)}
	 *
	 * @param bean
	 *            available bean
	 * @param beanName
	 *            bean name
	 * @return wrapped tracer or bean
	 * @throws BeansException
	 *             bean exception
	 */
	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof Tracer) {
			return JfrTracerFactory.create((Tracer) bean);
		}
		return bean;
	}

	/**
	 * see {@link BeanPostProcessor#postProcessBeforeInitialization(Object, String)}
	 * 
	 * @param bean
	 *            bean
	 * @param beanName
	 *            bean name
	 * @return bean
	 * @throws BeansException
	 *             bean exception
	 */
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

}
