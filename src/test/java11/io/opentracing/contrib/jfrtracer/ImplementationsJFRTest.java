package io.opentracing.contrib.jfrtracer;

import brave.Tracing;
import brave.opentracing.BraveTracer;
import brave.propagation.B3Propagation;
import brave.propagation.ExtraFieldPropagation;
import brave.propagation.Propagation.Factory;
import io.jaegertracing.Configuration.CodecConfiguration;
import io.jaegertracing.Configuration.ReporterConfiguration;
import io.jaegertracing.Configuration.SamplerConfiguration;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.contrib.jfrtracer.jfr.JFRScope;
import io.opentracing.contrib.jfrtracer.jfr.JFRSpan;
import jdk.jfr.Recording;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingFile;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static io.jaegertracing.Configuration.JAEGER_AGENT_HOST;
import static io.jaegertracing.Configuration.JAEGER_AGENT_PORT;
import static io.jaegertracing.Configuration.JAEGER_PROPAGATION;
import static io.jaegertracing.Configuration.JAEGER_SAMPLER_PARAM;
import static io.jaegertracing.Configuration.JAEGER_SAMPLER_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Disabled("Skip until Jaeger and Brave supportds 0.32.0")
public class ImplementationsJFRTest {

	@Test
	public void jaegerB3() throws IOException {
		System.setProperty(JAEGER_SAMPLER_TYPE, "const");
		System.setProperty(JAEGER_SAMPLER_PARAM, "1");
		System.setProperty(JAEGER_AGENT_HOST, "localhost");
		System.setProperty(JAEGER_AGENT_PORT, "6831");
		System.setProperty(JAEGER_PROPAGATION, "B3");

		Tracer jaegerTracer = new io.jaegertracing.Configuration("test")
				.withSampler(SamplerConfiguration.fromEnv())
				.withCodec(CodecConfiguration.fromEnv())
				.withReporter(ReporterConfiguration.fromEnv())
				.getTracer();
		innerTest(jaegerTracer);
	}

	@Test
	public void jaegerUber() throws IOException {
		System.setProperty(JAEGER_SAMPLER_TYPE, "const");
		System.setProperty(JAEGER_SAMPLER_PARAM, "1");
		System.setProperty(JAEGER_AGENT_HOST, "localhost");
		System.setProperty(JAEGER_AGENT_PORT, "6831");
		System.setProperty(JAEGER_PROPAGATION, "JAEGER");

		Tracer jaegerTracer = new io.jaegertracing.Configuration("test")
				.withSampler(SamplerConfiguration.fromEnv())
				.withCodec(CodecConfiguration.fromEnv())
				.withReporter(ReporterConfiguration.fromEnv())
				.getTracer();
		innerTest(jaegerTracer);
	}

	@Test
	public void brave() throws IOException {

		Factory propagationFactory = ExtraFieldPropagation.newFactoryBuilder(B3Propagation.FACTORY)
				.addPrefixedFields("baggage-", Arrays.asList("country-code", "user-id"))
				.build();

		Tracing braveTracing = Tracing.newBuilder()
				.localServiceName("my-service")
				.propagationFactory(propagationFactory)
				.build();
		innerTest(BraveTracer.create(braveTracing));
	}

	private void innerTest(Tracer testTracer) throws IOException {
		Path output = Files.createTempFile("test-recording", ".jfr");
		try {

			Tracer tracer = JFRTracer.wrap(testTracer);

			try (Recording recording = new Recording()) {
				recording.enable(JFRSpan.class);
				recording.enable(JFRScope.class);
				recording.start();

				// Generate span
				Span start = tracer.buildSpan("outer span").start();
				tracer.scopeManager().activate(start);
				tracer.activateSpan(tracer.buildSpan("inner span").start()).close();
				tracer.scopeManager().active().close();
				start.finish();

				recording.dump(output);
			}

			// Validate span was created and recorded in JFR
			List<RecordedEvent> events = RecordingFile.readAllEvents(output);
			assertEquals(4, events.size());
			events.stream()
					.forEach(e -> {
						assertNotNull(e.getString("name"));
						if (e.getString("name").equals("inner span")) {
							assertNotNull(e.getString("parentSpanId"));
						}
						assertNotNull(e.getString("traceId"));
						assertNotNull(e.getString("spanId"));
					});

		} finally {
			Files.delete(output);
		}
	}
}
