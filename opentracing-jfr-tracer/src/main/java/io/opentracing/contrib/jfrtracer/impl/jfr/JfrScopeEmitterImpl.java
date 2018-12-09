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
 * This is the JDK 8 implementation. For the JDK 11 and later implementation, see src/main/java11.
 */
@SuppressWarnings({"deprecation"})
final class JfrScopeEmitterImpl extends AbstractJfrEmitter {

	private static final Producer PRODUCER;
	private static final EventToken SCOPE_EVENT_TOKEN;

	static {
		URI producerURI = URI.create("http://opentracing.io/jfr-tracer");
		PRODUCER = new Producer("jfr-tracer", "Events produced by the OpenTracing jfr-tracer.", producerURI);
		PRODUCER.register();
		SCOPE_EVENT_TOKEN = register(ScopeEvent.class);
	}

	private ScopeEvent currentEvent;

	JfrScopeEmitterImpl(Span span) {
		super(span);
	}

	@Override
	public void close() {
		if (currentEvent != null) {
			if (currentEvent.shouldWrite()) {
				currentEvent.end();
				currentEvent.commit();
			}
			currentEvent = null;
		}
	}

	@Override
	public void start(String parentId, String operationName) {
		currentEvent = new ScopeEvent(SCOPE_EVENT_TOKEN);
		currentEvent.operationName = operationName;
		currentEvent.parentId = parentId;
		currentEvent.traceId = span.context().toTraceId();
		currentEvent.spanId = span.context().toSpanId();
		currentEvent.begin();
	}

	@Override
	public String toString() {
		return "JDK 8 JFR Scope Emitter";
	}

	/**
	 * Helper method to register an event class with the jfr-tracer producer.
	 *
	 * @param clazz the event class to register.
	 * @return the token associated with the event class.
	 */
	static EventToken register(Class<? extends InstantEvent> clazz) {
		try {
			EventToken token = PRODUCER.addEvent(clazz);
			LOGGER.fine("Registered EventType " + clazz.getName());
			return token;
		} catch (InvalidEventDefinitionException | InvalidValueException e) {
			LOGGER.log(Level.SEVERE,
					"Failed to register the event class " + clazz.getName()
					+ ". Event will not be available. Please check your configuration.",
					e);
		}
		return null;
	}

	@EventDefinition(path = "opentracing/scopeevent", name = "ScopeEvent", description = "A thread local event triggered by scope activation", stacktrace = true, thread = true)
	public static class ScopeEvent extends TimedEvent {

		@ValueDefinition(name = "Operation Name")
		private String operationName;

		@ValueDefinition(name = "Trace Id")
		private String traceId;

		@ValueDefinition(name = "Span Id")
		private String spanId;

		@ValueDefinition(name = "Parent Id")
		private String parentId;

		ScopeEvent(EventToken eventToken) {
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
}
