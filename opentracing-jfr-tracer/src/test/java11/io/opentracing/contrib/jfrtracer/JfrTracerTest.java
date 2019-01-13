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

import io.opentracing.Scope;
import io.opentracing.Tracer;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import jdk.jfr.FlightRecorder;
import jdk.jfr.Recording;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingFile;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JfrTracerTest {

	/**
	 * Test JFR gets the generated span
	 *
	 * @throws java.io.IOException
	 *             on error
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void basicEvent() throws IOException, InterruptedException {
		Path output = Files.createTempFile("test-recording-basic-11", ".jfr");
		try {
			// Setup tracers
			MockTracer mockTracer = new MockTracer();
			Tracer tracer = JfrTracerFactory.create(mockTracer);

			try (Recording recording = JfrTestUtils.startJFR()) {

				// Generate span
				tracer.buildSpan("test span").start().finish();

				recording.dump(output);
				recording.stop();
			}

			// To be removed when test are fixed, it's used due to concurrency issue
			Thread.sleep(100);

			// Validate span was created and recorded in JFR
			assertEquals(1, mockTracer.finishedSpans().size());

			Map<String, MockSpan> finishedSpans = mockTracer.finishedSpans().stream()
					.collect(Collectors.toMap(e -> e.operationName(), e -> e));
			List<RecordedEvent> events = RecordingFile.readAllEvents(output);
			assertEquals(finishedSpans.size(), events.size());
			events.stream().forEach(e -> {
				MockSpan finishedSpan = finishedSpans.get(e.getString("operationName"));
				assertNotNull(finishedSpan);
				assertEquals(Long.toString(finishedSpan.context().traceId()), e.getString("traceId"));
				assertEquals(Long.toString(finishedSpan.context().spanId()), e.getString("spanId"));
				assertEquals(finishedSpan.operationName(), e.getString("operationName"));
				assertTrue(e.getEventType().getName().contains("SpanEvent"));
			});

		} finally {
			JfrTestUtils.delete(output);
		}
	}

	@Test
	public void noJFR() throws IOException {
		// Setup tracers
		MockTracer mockTracer = new MockTracer();
		Tracer tracer = JfrTracerFactory.create(mockTracer);

		// Generate span
		assertNull(tracer.scopeManager().active());
		tracer.activateSpan(tracer.buildSpan("test span").start()).close();

		// Validate span was created and recorded in JFR
		assertEquals(1, mockTracer.finishedSpans().size());
		assertNull(tracer.scopeManager().active());
	}

	@Test
	@SuppressWarnings("try")
	public void noRunningJFR() throws IOException, InterruptedException {
		// Setup tracers
		MockTracer mockTracer = new MockTracer();
		Tracer tracer = JfrTracerFactory.create(mockTracer);

		Recording recording = JfrTestUtils.startJFR();

		// Generate span
		assertNull(tracer.scopeManager().active());
		try (Scope scope = tracer.activateSpan(tracer.buildSpan("outer span").start())) {
			Scope activeScopeOuter = tracer.scopeManager().active();
			assertNotNull(activeScopeOuter);
			recording.close();
			while (!FlightRecorder.getFlightRecorder().getRecordings().isEmpty()) {
				System.out.println(FlightRecorder.getFlightRecorder().getRecordings().size());
			}
			await().atMost(20, TimeUnit.SECONDS)
					.until(() -> FlightRecorder.getFlightRecorder().getRecordings().isEmpty());
			try (Scope inner = tracer.activateSpan(tracer.buildSpan("inner span").start())) {
				Scope activeScopeInner = tracer.scopeManager().active();
				assertNotNull(activeScopeInner);
				assertNotEquals(activeScopeOuter, activeScopeInner);
			}
		}
		try (Scope scope = tracer.activateSpan(tracer.buildSpan("separate span").start())) {
			Scope activeScope = tracer.scopeManager().active();
			assertNotNull(activeScope);
			assertFalse(activeScope.getClass().getSimpleName().contains("ScopeEvent"));
		}

		// Validate span was created and recorded in JFR
		assertEquals(3, mockTracer.finishedSpans().size());
		assertNull(tracer.scopeManager().active());
	}
}
