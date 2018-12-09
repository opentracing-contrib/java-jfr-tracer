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
package io.opentracing.contrib.jfrtracer.impl.jfr;

import com.oracle.jrockit.jfr.EventDefinition;
import com.oracle.jrockit.jfr.EventToken;
import com.oracle.jrockit.jfr.TimedEvent;
import com.oracle.jrockit.jfr.ValueDefinition;

import io.opentracing.Span;

/**
 * This is the JDK 8 implementation for emitting Span events. For the JDK 11 and later
 * implementation, see src/main/java11.
 */
@SuppressWarnings("deprecation")
final class JfrSpanEmitterImpl extends AbstractJfrSpanEmitter {

	private static final EventToken SPAN_EVENT_TOKEN;

	static {
		SPAN_EVENT_TOKEN = JfrScopeEmitterImpl.register(SpanEvent.class);
	}

	private SpanEvent currentEvent;

	JfrSpanEmitterImpl(Span span) {
		super(span);
	}

	@Override
	public void start(String parentId, String operationName) {
		currentEvent = new SpanEvent(SPAN_EVENT_TOKEN);
		if (currentEvent.getEventInfo().isEnabled()) {
			currentEvent.operationName = operationName;
			currentEvent.traceId = span.context().toTraceId();
			currentEvent.spanId = span.context().toSpanId();
			currentEvent.parentId = parentId;
			currentEvent.startThread = Thread.currentThread();
		}
		EXECUTOR.execute(new BeginEventCommand(currentEvent));
	}

	@Override
	public void close() {
		if (currentEvent != null) {
			currentEvent.endThread = Thread.currentThread();
			EXECUTOR.execute(new EndEventCommand(currentEvent));
			currentEvent = null;
		}
	}

	@Override
	public String toString() {
		return "JDK 8 JFR Span Emitter";
	}

	// Must be public for JFR to access it
	@EventDefinition(path = "opentracing/spanevent", name = "SpanEvent", description = "And event representing an OpenTracing span", stacktrace = false, thread = true)
	public static class SpanEvent extends TimedEvent {

		@ValueDefinition(name = "Operation Name")
		private String operationName;

		@ValueDefinition(name = "Trace Id")
		private String traceId;

		@ValueDefinition(name = "Span Id")
		private String spanId;

		@ValueDefinition(name = "Parent Id")
		private String parentId;

		@ValueDefinition(name = "Start Thread", description = "The thread initiating the span")
		private Thread startThread;

		@ValueDefinition(name = "End Thread", description = "The thread ending the span")
		private Thread endThread;

		SpanEvent(EventToken eventToken) {
			super(eventToken);
		}

		@SuppressWarnings("unused")
		public String getTraceId() {
			return traceId;
		}

		@SuppressWarnings("unused")
		public String getSpanId() {
			return spanId;
		}

		@SuppressWarnings("unused")
		public String getParentId() {
			return parentId;
		}

		@SuppressWarnings("unused")
		public Thread getStartThread() {
			return startThread;
		}

		@SuppressWarnings("unused")
		public Thread getEndThread() {
			return endThread;
		}

		@SuppressWarnings("unused")
		public String getOperationName() {
			return operationName;
		}
	}

	private static class EndEventCommand implements Runnable {

		private final SpanEvent event;

		EndEventCommand(SpanEvent event) {
			this.event = event;
		}

		@Override
		public void run() {
			if (event.shouldWrite()) {
				event.end();
				event.commit();
			}
		}
	}

	private static class BeginEventCommand implements Runnable {

		private final SpanEvent event;

		BeginEventCommand(SpanEvent event) {
			this.event = event;
		}

		@Override
		public void run() {
			event.begin();
		}
	}
}
