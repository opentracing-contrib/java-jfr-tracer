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
package se.hirt.jmc.opentracing.extractors;

import io.opentracing.Span;
import io.opentracing.Tracer;
import se.hirt.jmc.opentracing.ContextExtractor;
import brave.opentracing.BraveSpan;
import brave.opentracing.BraveTracer;

/**
 * Extractor for the Open Zipkin/Brave tracer.
 * 
 * @see ContextExtractor
 * @author Marcus Hirt
 */
public class ZipkinContextExtractor implements ContextExtractor {

	/*
	 * Not easily extracted in Zipkin.
	 */
	@Override
	public String extractOperationName(Span span) {
		return "";
	}

	@Override
	public String extractTraceId(Span span) {
		return ((BraveSpan) span).unwrap().context().traceIdString();
	}

	@Override
	public String extractSpanId(Span span) {
		return String.valueOf(((BraveSpan) span).unwrap().context().spanId());
	}

	@Override
	public String extractParentId(Span span) {
		return String.valueOf(((BraveSpan) span).unwrap().context().parentId());
	}

	@Override
	public Class<? extends Span> getSupportedSpanType() {
		return BraveSpan.class;
	}

	@Override
	public Class<? extends Tracer> getSupportedTracerType() {
		return BraveTracer.class;
	}

}
