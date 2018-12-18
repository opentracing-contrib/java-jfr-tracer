package io.opentracing.contrib.jfrtracer.spring.autoconfigure;

import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import io.opentracing.contrib.jfrtracer.spring.autoconfigure.jaeger.AbstractJaegerTracerSpringTest;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SimpleSpringBooWebApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = AbstractJaegerTracerSpringTest.Initializer.class)
public class JfrTracerIntegrationTests extends AbstractJaegerTracerSpringTest {

	@Autowired
	private TestRestTemplate testRestTemplate;

	@Test
	public void testJaegerCollectsTraces() {
		final String operation = "jfr";
		Assertions.assertThat(testRestTemplate.getForObject("/" + operation, String.class)).isNotBlank();
		waitJaegerQueryContains(operation);
	}

	private void waitJaegerQueryContains(String str) {
		final RestTemplate restTemplate = new RestTemplateBuilder().build();
		Awaitility.await().atMost(30, TimeUnit.SECONDS).until(() -> {
			try {
				final String output = restTemplate.getForObject(String.format("%s/api/traces?service=%s",
						String.format("http://%s:%d", UDP_SENDER_HOST, jaeger.getMappedPort(QUERY_PORT)), SERVICE_NAME),
						String.class);
				return output != null && output.contains(str);
			} catch (Exception e) {
				return false;
			}
		});
	}

}
