package io.opentracing.contrib.jfrtracer.spring.autoconfigure;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import io.opentracing.Tracer;
import io.opentracing.contrib.jfrtracer.JfrTracerFactory;

@Configuration
//@ConditionalOnProperty(value = "opentracing.jfr-tracer.enabled", havingValue = "true", matchIfMissing = true)
public class JfrTracerBeanPostProcessor implements BeanPostProcessor {

	private static final Log log = LogFactory.getLog(JfrTracerBeanPostProcessor.class);
	private Tracer jfrTracer;

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof Tracer) {
			log.info("RE-INIT BEAN:" + beanName + " class: " + bean.getClass().getSimpleName());
			jfrTracer = JfrTracerFactory.create((Tracer) bean);
			return jfrTracer;
//			return bean;
		}
		return bean;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

}
