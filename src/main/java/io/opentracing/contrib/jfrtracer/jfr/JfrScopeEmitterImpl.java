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
package io.opentracing.contrib.jfrtracer.jfr;

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

/**
 * This is the JDK 7/8 implementation. For the JDK 9 and later implementation, see src/main/java9.
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

	JfrScopeEmitterImpl(Span span) {
		super(span);
	}

	@Override
	public void close() {
		if (currentEvent != null) {
			currentEvent.end();
			currentEvent.commit();
			currentEvent = null;
		} else {
			LOGGER.warning("Close without start discovered!");
		}
	}

	@Override
	public void start(String operationName) {
		currentEvent = new ScopeEvent(SCOPE_EVENT_TOKEN);
		currentEvent.operationName = operationName;
		currentEvent.traceId = span.context().toTraceId();
		currentEvent.spanId = span.context().toSpanId();
		// currentEvent.parentId = span.context().toParentId();
		currentEvent.begin();
	}

	@Override
	public String toString() {
		return "JDK 7 & JDK 8 JFR Scope Emitter";
	}
}
