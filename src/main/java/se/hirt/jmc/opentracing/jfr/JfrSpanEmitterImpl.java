/*
 * Copyright (c) 2018, Marcus Hirt
 * 
 * jfr-tracer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jfr-tracer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jfr-tracer. If not, see <http://www.gnu.org/licenses/>.
 */
package se.hirt.jmc.opentracing.jfr;

import java.net.URI;

import com.oracle.jrockit.jfr.EventDefinition;
import com.oracle.jrockit.jfr.EventToken;
import com.oracle.jrockit.jfr.Producer;
import com.oracle.jrockit.jfr.TimedEvent;
import com.oracle.jrockit.jfr.ValueDefinition;

import io.opentracing.Span;
import se.hirt.jmc.opentracing.ContextExtractor;

/**
 * This is the JDK 7/8 implementation for emitting Span events. For the JDK 9 and later
 * implementation, see src/main/java9.
 * 
 * @author Marcus Hirt
 */
@SuppressWarnings("deprecation")
final class JfrSpanEmitterImpl extends AbstractJfrSpanEmitterImpl {
	private static final Producer PRODUCER;
	private static final EventToken SPAN_EVENT_TOKEN;
	private SpanEvent currentEvent;

	static {
		URI producerURI = URI.create("http://hirt.se/jfr-tracer");
		PRODUCER = new Producer("jfr-tracer", "Events produced by the OpenTracing jfr-tracer.", producerURI);
		PRODUCER.register();
		SPAN_EVENT_TOKEN = JfrScopeEmitterImpl.register(SpanEvent.class);
	}

	@EventDefinition(path = "jfrtracer/spanevent", name = "SpanEvent", description = "And event representing a span", stacktrace = false, thread = true)
	private static class SpanEvent extends TimedEvent {
		@ValueDefinition(name = "TraceId", description = "The trace identifier for this event")
		private String traceId;

		@ValueDefinition(name = "SpanId", description = "The span identifier for this event")
		private String spanId;

		@ValueDefinition(name = "ParentId", description = "The parent span identifier for this event")
		private String parentId;

		public SpanEvent(EventToken eventToken) {
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
	}

	private static class EndEventCommand implements Runnable {
		private final SpanEvent event;

		public EndEventCommand(SpanEvent event) {
			this.event = event;
		}

		@Override
		public void run() {
			event.end();
			event.commit();
		}
	}

	private static class BeginEventCommand implements Runnable {
		private final SpanEvent event;

		public BeginEventCommand(SpanEvent event) {
			this.event = event;
		}

		@Override
		public void run() {
			event.begin();
		}
	}

	JfrSpanEmitterImpl(Span span, ContextExtractor extractor) {
		super(span, extractor);
	}

	@Override
	public void close() throws Exception {
		if (currentEvent != null) {
			EXECUTOR.execute(new EndEventCommand(currentEvent));
			currentEvent = null;
		} else {
			LOGGER.warning("Close without start discovered!");
		}
	}

	@Override
	public void start() {
		currentEvent = new SpanEvent(SPAN_EVENT_TOKEN);
		if (extractor != null) {
			currentEvent.traceId = extractor.extractTraceId(span);
			currentEvent.spanId = extractor.extractSpanId(span);
			currentEvent.parentId = extractor.extractParentId(span);
		} else {
			LOGGER.warning(
					"Trying to create event when no valid extractor is available. Create an extractor for your particular open tracing tracer implementation, and register it with the ExtractorRegistry.");
		}
		EXECUTOR.execute(new BeginEventCommand(currentEvent));
	}

	@Override
	public String toString() {
		return "JDK 7 & JDK 8 JFR Span Emitter for " + extractor.getSupportedTracerType() + "/"
				+ extractor.getSupportedSpanType();
	}
}
