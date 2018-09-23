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

import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Category;
import jdk.jfr.Description;

import io.opentracing.Span;
import se.hirt.jmc.opentracing.ContextExtractor;
import se.hirt.jmc.opentracing.extractors.ExtractorRegistry;

/**
 * This is the JDK 9 or later implementation of the JfrEmitter.
 * 
 * @author Marcus Hirt
 */
public class JfrScopeEmitterImpl extends AbstractJfrEmitterImpl {
	private Jdk9ScopeEvent currentEvent;

	@Label("Scope Event")
	@Description("Open tracing event corresponding to an activation scope.")
	@Category("Open Tracing")
	private static class Jdk9ScopeEvent extends Event {
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
		currentEvent = new Jdk9ScopeEvent();
		if (extractor != null) {
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
		return "JDK 9+ JFR Emitter for " + extractor.getSupportedTracerType() + "/" + extractor.getSupportedSpanType();
	}
}
