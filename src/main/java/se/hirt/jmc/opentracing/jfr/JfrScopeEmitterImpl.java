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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.oracle.jrockit.jfr.EventDefinition;
import com.oracle.jrockit.jfr.EventToken;
import com.oracle.jrockit.jfr.InstantEvent;
import com.oracle.jrockit.jfr.InvalidEventDefinitionException;
import com.oracle.jrockit.jfr.InvalidValueException;
import com.oracle.jrockit.jfr.Producer;
import com.oracle.jrockit.jfr.TimedEvent;
import com.oracle.jrockit.jfr.ValueDefinition;

import io.opentracing.Span;
import se.hirt.jmc.opentracing.ContextExtractor;

/**
 * This is the JDK 7/8 implementation. For the JDK 9 and later implementation, see src/main/java9.
 * 
 * @author Marcus Hirt
 */
@SuppressWarnings("deprecation")
final class JfrScopeEmitterImpl extends AbstractJfrEmitterImpl {
	private static final Producer PRODUCER;
	private static final EventToken SCOPE_EVENT_TOKEN;
	private ScopeEvent currentEvent;

	static {
		URI producerURI = URI.create("http://hirt.se/jfr-tracer");
		PRODUCER = new Producer("jfr-tracer", "Events produced by the OpenTracing jfr-tracer.", producerURI);
		PRODUCER.register();
		SCOPE_EVENT_TOKEN = register(ScopeEvent.class);
	}

	@EventDefinition(path = "jfrtracer/scopeevent", name = "ScopeEvent", description = "A thread local event triggered by scope activation", stacktrace = true, thread = true)
	private static class ScopeEvent extends TimedEvent {
		@ValueDefinition(name = "OperationName", description = "The operationName for the span active in this scope")
		private String operationName;

		@ValueDefinition(name = "TraceId", description = "The trace identifier for this event")
		private String traceId;

		@ValueDefinition(name = "SpanId", description = "The span identifier for this event")
		private String spanId;

		@ValueDefinition(name = "ParentId", description = "The parent span identifier for this event")
		private String parentId;

		public ScopeEvent(EventToken eventToken) {
			super(eventToken);
		}

		@SuppressWarnings("unused")
		public String getOperationName() {
			return operationName;
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

	/**
	 * Helper method to register an event class with the jfr-tracer producer.
	 *
	 * @param clazz
	 *            the event class to register.
	 * @return the token associated with the event class.
	 */
	static EventToken register(Class<? extends InstantEvent> clazz) {
		try {
			EventToken token = PRODUCER.addEvent(clazz);
			Logger.getLogger(JfrScopeEmitterImpl.class.getName()).log(Level.FINE,
					"Registered EventType " + clazz.getName());
			return token;
		} catch (InvalidEventDefinitionException | InvalidValueException e) {
			Logger.getLogger(JfrScopeEmitterImpl.class.getName()).log(Level.SEVERE,
					"Failed to register the event class " + clazz.getName()
							+ ". Event will not be available. Please check your configuration.",
					e);
		}
		return null;
	}

	JfrScopeEmitterImpl(Span span, ContextExtractor extractor) {
		super(span, extractor);
	}

	@Override
	public void close() throws Exception {
		if (currentEvent != null) {
			currentEvent.end();
			currentEvent.commit();
			currentEvent = null;
		} else {
			LOGGER.warning("Close without start discovered!");
		}
	}

	@Override
	public void start() {
		currentEvent = new ScopeEvent(SCOPE_EVENT_TOKEN);
		if (extractor != null) {
			currentEvent.operationName = extractor.extractOperationName(span);
			currentEvent.traceId = extractor.extractTraceId(span);
			currentEvent.spanId = extractor.extractSpanId(span);
			currentEvent.parentId = extractor.extractParentId(span);
		} else {
			LOGGER.warning(
					"Trying to create event when no valid extractor is available. Create an extractor for your particular open tracing tracer implementation, and register it with the ExtractorRegistry.");
		}
		currentEvent.begin();
	}

	@Override
	public String toString() {
		return "JDK 7 & JDK 8 JFR Scope Emitter for " + extractor.getSupportedTracerType() + "/"
				+ extractor.getSupportedSpanType();
	}
}
