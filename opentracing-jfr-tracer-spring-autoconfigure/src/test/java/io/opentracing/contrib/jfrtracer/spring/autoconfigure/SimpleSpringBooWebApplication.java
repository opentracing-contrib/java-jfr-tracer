package io.opentracing.contrib.jfrtracer.spring.autoconfigure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.opentracing.contrib.jfrtracer.spring.autoconfigure.jaeger.AbstractJaegerTracerSpringTest;

@SpringBootApplication
@RestController
@ContextConfiguration(initializers = AbstractJaegerTracerSpringTest.Initializer.class)
public class SimpleSpringBooWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(SimpleSpringBooWebApplication.class, args);
	}

	@GetMapping("/jfr")
	public String hello() {
		return "hello jfr";
	}
}
