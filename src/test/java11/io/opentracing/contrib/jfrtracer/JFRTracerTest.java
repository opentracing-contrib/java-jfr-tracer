package io.opentracing.contrib.jfrtracer;

import io.opentracing.Scope;
import io.opentracing.Tracer;
import io.opentracing.contrib.jfrtracer.JFRTracer;
import io.opentracing.contrib.jfrtracer.jfr.JFRScope;
import io.opentracing.contrib.jfrtracer.jfr.JFRSpan;
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

public class JFRTracerTest {

	/**
	 * Test JFR gets the generated span
	 *
	 * @throws java.io.IOException on error
	 */
	@Test
	public void basicEvent() throws IOException {
		Path output = Files.createTempFile("test-recording", ".jfr");
		try {
			// Setup tracers
			MockTracer mockTracer = new MockTracer();
			Tracer tracer = JFRTracer.wrap(mockTracer);

			try (Recording recording = new Recording()) {
				recording.enable(JFRSpan.class);
				recording.start();

				// Generate span
				tracer.buildSpan("test span").start().finish();

				recording.dump(output);
			}

			// Validate span was created and recorded in JFR
			assertEquals(1, mockTracer.finishedSpans().size());

			Map<String, MockSpan> finishedSpans = mockTracer.finishedSpans().stream().collect(Collectors.toMap(e -> e.operationName(), e -> e));
			List<RecordedEvent> events = RecordingFile.readAllEvents(output);
			assertEquals(finishedSpans.size(), events.size());
			events.stream()
					.forEach(e -> {
						MockSpan finishedSpan = finishedSpans.get(e.getString("name"));
						assertNotNull(finishedSpan);
						assertEquals(Long.toString(finishedSpan.context().traceId()), e.getString("traceId"));
						assertEquals(Long.toString(finishedSpan.context().spanId()), e.getString("spanId"));
						assertEquals(finishedSpan.operationName(), e.getString("name"));
					});

		} finally {
			Files.delete(output);
		}
	}

	@Test
	public void noJFR() throws IOException {
		// Setup tracers
		MockTracer mockTracer = new MockTracer();
		Tracer tracer = JFRTracer.wrap(mockTracer);

		// Generate span
		assertNull(tracer.scopeManager().active());
		tracer.buildSpan("test span").startActive(true).close();

		// Validate span was created and recorded in JFR
		assertEquals(1, mockTracer.finishedSpans().size());
		assertNull(tracer.scopeManager().active());
	}

	@Test
	@SuppressWarnings("try")
	public void noRunningJFR() throws IOException, InterruptedException {
		// Setup tracers
		MockTracer mockTracer = new MockTracer();
		Tracer tracer = JFRTracer.wrap(mockTracer);

		Recording recording = new Recording();
		recording.enable(JFRSpan.class);
		recording.start();

		// Generate span
		assertNull(tracer.scopeManager().active());
		try (Scope scope = tracer.buildSpan("outer span").startActive(true)) {
			Scope activeScopeOuter = tracer.scopeManager().active();
			assertTrue(activeScopeOuter instanceof JFRScope);
			assertNotNull(activeScopeOuter);
			recording.close();
			while (!FlightRecorder.getFlightRecorder().getRecordings().isEmpty()) {
				System.out.println(FlightRecorder.getFlightRecorder().getRecordings().size());
			}
			await().atMost(20, TimeUnit.SECONDS).until(() -> FlightRecorder.getFlightRecorder().getRecordings().isEmpty());
			try (Scope inner = tracer.buildSpan("inner span").startActive(true)) {
				Scope activeScopeInner = tracer.scopeManager().active();
				assertNotNull(activeScopeInner);
				assertFalse(activeScopeInner instanceof JFRScope);
				assertNotEquals(activeScopeOuter, activeScopeInner);
			}
		}
		try (Scope scope = tracer.buildSpan("separate span").startActive(true)) {
			Scope activeScope = tracer.scopeManager().active();
			assertNotNull(activeScope);
			assertFalse(activeScope instanceof JFRScope);
		}

		// Validate span was created and recorded in JFR
		assertEquals(3, mockTracer.finishedSpans().size());
		assertNull(tracer.scopeManager().active());
	}
}
