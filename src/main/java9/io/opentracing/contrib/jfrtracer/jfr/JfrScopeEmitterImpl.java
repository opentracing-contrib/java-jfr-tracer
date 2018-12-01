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

import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Category;
import jdk.jfr.Description;

import io.opentracing.Span;
import io.opentracing.contrib.jfrtracer.jfr.AbstractJfrEmitterImpl;

/**
 * This is the JDK 9 or later implementation of the JfrEmitter.
 */
public class JfrScopeEmitterImpl extends AbstractJfrEmitterImpl {
	private Jdk9ScopeEvent currentEvent;

	@Label("Scope Event")
	@Description("Open tracing event corresponding to an activation scope")
	@Category("Open Tracing")
	private static class Jdk9ScopeEvent extends Event {
		@Label("Operation Name")
		@Description("The operation name for the span")
		private String operationName;

		@Label("Trace Id")
		@Description("The trace id for the span")
		private String traceId;

		@Label("Span Id")
		@Description("The id of the parent span")
		private String spanId;

		@Label("Parent Id")
		@Description("The id of the parent span")
		private String parentId;
	}

	JfrScopeEmitterImpl(Span span) {
		super(span);
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
	public void start(String operationName) {
		currentEvent = new Jdk9ScopeEvent();
		if (currentEvent.isEnabled()) {
			currentEvent.operationName = operationName;
			currentEvent.traceId = span.context().toTraceId();
			currentEvent.spanId = span.context().toSpanId();
			// currentEvent.parentId = span.context().toParentId();
		}
		currentEvent.begin();
	}

	@Override
	public String toString() {
		return "JDK 9+ JFR Emitter";
	}
}
