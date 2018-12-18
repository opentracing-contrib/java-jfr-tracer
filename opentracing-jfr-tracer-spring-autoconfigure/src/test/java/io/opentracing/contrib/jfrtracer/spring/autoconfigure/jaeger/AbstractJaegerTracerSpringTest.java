package io.opentracing.contrib.jfrtracer.spring.autoconfigure.jaeger;

import org.junit.ClassRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;

import io.opentracing.Tracer;
import io.opentracing.contrib.java.spring.jaeger.starter.JaegerAutoConfiguration;

@SpringBootTest(classes = {JaegerAutoConfiguration.class})
public abstract class AbstractJaegerTracerSpringTest {

	public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		@Override
		public void initialize(ConfigurableApplicationContext applicationContext) {
			TestPropertyValues.of(
					String.format("opentracing.jaeger.http-sender.url=http://%s:%d/api/traces",
							jaeger.getContainerIpAddress(), jaeger.getMappedPort(COLLECTOR_PORT)),
					String.format("opentracing.jaeger.udp-sender.host=%s", UDP_SENDER_HOST),
					String.format("opentracing.jaeger.udp-sender.port=%s", UDP_SENDER_PORT),
					String.format("spring.application.name=%s", SERVICE_NAME)).applyTo(applicationContext);
		}
	}

	private static final Integer UDP_SENDER_PORT = 6831;
	private static final int COLLECTOR_PORT = 14268;
	protected static final String SERVICE_NAME = "spring-boot-integration-test";
	protected static final String UDP_SENDER_HOST = "localhost";
	protected static final Integer QUERY_PORT = 16686;

	@ClassRule
	public static GenericContainer jaeger = new GenericContainer("jaegertracing/all-in-one:latest")
			.withExposedPorts(COLLECTOR_PORT, QUERY_PORT, UDP_SENDER_PORT)
			.waitingFor(new HttpWaitStrategy().forPath("/"));

	@Autowired(required = false)
	protected Tracer tracer;
}
