/*
 * Copyright 2018 The OpenTracing Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.opentracing.contrib.jfrtracer;

import static io.jaegertracing.Configuration.JAEGER_AGENT_HOST;
import static io.jaegertracing.Configuration.JAEGER_AGENT_PORT;
import static io.jaegertracing.Configuration.JAEGER_PROPAGATION;
import static io.jaegertracing.Configuration.JAEGER_SAMPLER_PARAM;
import static io.jaegertracing.Configuration.JAEGER_SAMPLER_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

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
import jdk.jfr.Recording;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingFile;

@Disabled("Skip until Jaeger and Brave supports 0.32.0")
public class ImplementationsJFRTest {

	@Test
	public void jaegerB3() throws IOException {
		System.setProperty(JAEGER_SAMPLER_TYPE, "const");
		System.setProperty(JAEGER_SAMPLER_PARAM, "1");
		System.setProperty(JAEGER_AGENT_HOST, "localhost");
		System.setProperty(JAEGER_AGENT_PORT, "6831");
		System.setProperty(JAEGER_PROPAGATION, "B3");

		Tracer jaegerTracer = new io.jaegertracing.Configuration("test").withSampler(SamplerConfiguration.fromEnv())
				.withCodec(CodecConfiguration.fromEnv()).withReporter(ReporterConfiguration.fromEnv()).getTracer();
		innerTest(jaegerTracer);
	}

	@Test
	public void jaegerUber() throws IOException {
		System.setProperty(JAEGER_SAMPLER_TYPE, "const");
		System.setProperty(JAEGER_SAMPLER_PARAM, "1");
		System.setProperty(JAEGER_AGENT_HOST, "localhost");
		System.setProperty(JAEGER_AGENT_PORT, "6831");
		System.setProperty(JAEGER_PROPAGATION, "JAEGER");

		Tracer jaegerTracer = new io.jaegertracing.Configuration("test").withSampler(SamplerConfiguration.fromEnv())
				.withCodec(CodecConfiguration.fromEnv()).withReporter(ReporterConfiguration.fromEnv()).getTracer();
		innerTest(jaegerTracer);
	}

	@Test
	public void brave() throws IOException {

		Factory propagationFactory = ExtraFieldPropagation.newFactoryBuilder(B3Propagation.FACTORY)
				.addPrefixedFields("baggage-", Arrays.asList("country-code", "user-id")).build();

		Tracing braveTracing = Tracing.newBuilder().localServiceName("my-service")
				.propagationFactory(propagationFactory).build();
		innerTest(BraveTracer.create(braveTracing));
	}

	private void innerTest(Tracer testTracer) throws IOException {
		Path output = Files.createTempFile("test-recording", ".jfr");
		try {

			Tracer tracer = JfrTracerFactory.create(testTracer);

			try (Recording recording = JfrTestUtils.startJFR()) {

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
			events.stream().forEach(e -> {
				assertNotNull(e.getString("operationName"));
				if (e.getString("operationName").equals("inner span")) {
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
