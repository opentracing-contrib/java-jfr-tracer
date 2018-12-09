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

import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Category;
import jdk.jfr.Description;

import io.opentracing.Span;

/**
 * This is the JDK 9 or later implementation of the JfrEmitter.
 */
public class JfrScopeEmitterImpl extends AbstractJfrEmitter {

	private ScopeEvent currentEvent;

	@Category("Open Tracing")
	@Label("Scope Event")
	@Description("Open tracing event corresponding to an activation scope")
	private static class ScopeEvent extends Event {

		@Label("Operation Name")
		private String operationName;

		@Label("Trace Id")
		private String traceId;

		@Label("Span Id")
		private String spanId;

		@Label("Parent Id")
		private String parentId;
	}

	JfrScopeEmitterImpl(Span span) {
		super(span);
	}

	@Override
	public void close() {
		if (currentEvent != null) {
			if (currentEvent.shouldCommit()) {
				currentEvent.end();
				currentEvent.commit();
			}
			currentEvent = null;
		}
	}

	@Override
	public void start(String parentId, String operationName) {
		currentEvent = new ScopeEvent();
		if (currentEvent.isEnabled()) {
			currentEvent.operationName = operationName;
			currentEvent.parentId = parentId;
			currentEvent.traceId = span.context().toTraceId();
			currentEvent.spanId = span.context().toSpanId();
		}
		currentEvent.begin();
	}

	@Override
	public String toString() {
		return "JDK 11 JFR Emitter";
	}
}
