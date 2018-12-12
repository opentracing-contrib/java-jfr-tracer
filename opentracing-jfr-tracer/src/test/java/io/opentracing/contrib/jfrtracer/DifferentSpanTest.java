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

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.contrib.concurrent.TracedExecutorService;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import oracle.jrockit.jfr.parser.FLREvent;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SuppressWarnings("deprecation")
public class DifferentSpanTest {

	@Test
	public void spansInMultipleThreads()
			throws IOException, InterruptedException, ExecutionException, TimeoutException {
		Path output = Files.createTempFile("test-recording", ".jfr");

		try {
			// Setup tracers
			MockTracer mockTracer = new MockTracer();
			Tracer tracer = JfrTracerFactory.create(mockTracer);

			// Start JFR
			JFRTestUtils.startJFR();

			// Generate spans
			Span span = tracer.buildSpan("test span").start();
			TracedExecutorService executor = new TracedExecutorService(Executors.newSingleThreadExecutor(), tracer);
			executor.submit(() -> {
				tracer.buildSpan("executor span").start().finish();
			}).get(5, TimeUnit.SECONDS);
			span.finish();

			// Stop recording
			//Ugly Sleep for now
			Thread.sleep(100);
			List<FLREvent> events = JFRTestUtils.stopJfr(output);

			// Validate span was created and recorded in JFR
			assertEquals(2, mockTracer.finishedSpans().size());

			Map<String, MockSpan> finishedSpans = mockTracer.finishedSpans().stream()
					.collect(Collectors.toMap(e -> e.operationName(), e -> e));
			assertEquals(finishedSpans.size(), events.size());
			events.stream().forEach(e -> {
				MockSpan finishedSpan = finishedSpans.get(e.getValue("operationName").toString());
				assertNotNull(finishedSpan);
				assertEquals(Long.toString(finishedSpan.context().traceId()), e.getValue("traceId"));
				assertEquals(Long.toString(finishedSpan.context().spanId()), e.getValue("spanId"));
				assertEquals(finishedSpan.operationName(), e.getValue("operationName"));
			});

		} finally {
			Files.delete(output);
		}
	}

	@Test
	public void passingSpanBetweenThreads()
			throws IOException, InterruptedException, TimeoutException, ExecutionException {
		Path output = Files.createTempFile("test-recording", ".jfr");

		try {
			// Setup tracers
			MockTracer mockTracer = new MockTracer();
			Tracer tracer = JfrTracerFactory.create(mockTracer);

			// Start JFR
			JFRTestUtils.startJFR();

			// Generate spans
			TracedExecutorService executor = new TracedExecutorService(Executors.newSingleThreadExecutor(), tracer);
			long expectedStartThread = Thread.currentThread().getId();
			Span span = tracer.buildSpan("test span").start();
			long expectedFinishThread = executor.submit(() -> {
				span.finish();
				return Thread.currentThread().getId();
			}).get(5, TimeUnit.SECONDS);

			// Stop recording
			Thread.sleep(100);
			List<FLREvent> events = JFRTestUtils.stopJfr(output);

			// Validate span was created and recorded in JFR
			assertEquals(1, mockTracer.finishedSpans().size());

			Map<String, MockSpan> finishedSpans = mockTracer.finishedSpans().stream()
					.collect(Collectors.toMap(e -> e.operationName(), e -> e));
			assertEquals(finishedSpans.size(), events.size());
			events.stream().forEach(e -> {
				MockSpan finishedSpan = finishedSpans.get(e.getValue("operationName").toString());
				assertNotNull(finishedSpan);
				assertEquals(Long.toString(finishedSpan.context().traceId()), e.getValue("traceId"));
				assertEquals(Long.toString(finishedSpan.context().spanId()), e.getValue("spanId"));
				assertEquals(finishedSpan.operationName(), e.getValue("operationName"));
				assertNotEquals(expectedStartThread, e.getThread());
				assertNotEquals(expectedFinishThread, e.getThread());
				assertEquals(expectedStartThread, e.getValue("startThread"));
				assertEquals(expectedFinishThread, e.getValue("endThread"));
			});

		} finally {
			Files.delete(output);
		}
	}
}
