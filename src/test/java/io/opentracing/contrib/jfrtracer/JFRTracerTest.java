package io.opentracing.contrib.jfrtracer;

import io.opentracing.Tracer;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import oracle.jrockit.jfr.parser.FLREvent;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.opentracing.contrib.jfrtracer.JFRTestUtils.getJfrConfig;
import static io.opentracing.contrib.jfrtracer.JFRTestUtils.startJFR;
import static io.opentracing.contrib.jfrtracer.JFRTestUtils.stopJfr;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JFRTracerTest {

	/**
	 * Test JFR gets the generated span
	 *
	 * @throws java.io.IOException on error
	 */
	@Test
	@SuppressWarnings("deprecation")
	public void basicEvent() throws IOException {
		Path jfrConfig = getJfrConfig();
		Path output = Files.createTempFile("opentracing", ".jfr");

		try {

			// Setup tracers
			MockTracer mockTracer = new MockTracer();
			Tracer tracer = JFRTracer.wrap(mockTracer);
			// Start JFR
			startJFR(jfrConfig);

			// Generate span
			tracer.buildSpan("test span").start().finish();

			// Stop recording
			List<FLREvent> events = stopJfr(output);

			// Validate span was created and recorded in JFR
			assertEquals(1, mockTracer.finishedSpans().size());

			Map<String, MockSpan> finishedSpans = mockTracer.finishedSpans().stream().collect(Collectors.toMap(e -> e.operationName(), e -> e));
			assertEquals(finishedSpans.size(), events.size());
			events.stream()
					.forEach(e -> {
						MockSpan finishedSpan = finishedSpans.get(e.getValue("operationName").toString());
						assertNotNull(finishedSpan);
						assertEquals(Long.toString(finishedSpan.context().traceId()), e.getValue("traceId"));
						assertEquals(Long.toString(finishedSpan.context().spanId()), e.getValue("spanId"));
						assertEquals(finishedSpan.operationName(), e.getValue("operationName"));
					});

		} finally {
			Files.delete(jfrConfig);
			Files.delete(output);
		}
	}

	@Test
	public void noJFR() throws IOException {
		// Setup tracers
		MockTracer mockTracer = new MockTracer();
		Tracer tracer = JFRTracer.wrap(mockTracer);

		// Generate span
		tracer.buildSpan("test span").start().finish();

		// Validate span was created and recorded in JFR
		assertEquals(1, mockTracer.finishedSpans().size());
	}
}
