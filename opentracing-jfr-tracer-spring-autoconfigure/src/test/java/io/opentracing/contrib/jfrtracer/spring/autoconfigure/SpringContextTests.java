package io.opentracing.contrib.jfrtracer.spring.autoconfigure;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import io.opentracing.Tracer;
import io.opentracing.mock.MockTracer;

@SpringBootTest
@RunWith(SpringRunner.class)
public class SpringContextTests {

	private static final MockTracer mockTracer = new MockTracer();

	@Bean
	public Tracer tracer() {
		return mockTracer;
	}

	@Autowired
	private ApplicationContext context;

	@Test
	public void contextTest() {
		Assert.assertNotNull(context);
		Assert.assertTrue(context.containsBean("tracer"));
	}
}
