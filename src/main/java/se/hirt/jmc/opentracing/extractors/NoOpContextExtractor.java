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
import se.hirt.jmc.opentracing.noop.NoOpTracer;
import se.hirt.jmc.opentracing.noop.NoOpTracer.NoOpSpan;

/**
 * Extractor for the NoOpTracer.
 * 
 * @see NoOpTracer
 * @author Marcus Hirt
 */
public class NoOpContextExtractor implements ContextExtractor {
	
	@Override
	public String extractOperationName(Span span) {
		return ((NoOpSpan) span).getOperationName();
	}

	@Override
	public String extractTraceId(Span span) {
		return String.format("%x", ((NoOpSpan) span).getTraceId());
	}

	@Override
	public String extractSpanId(Span span) {
		return String.format("%x", ((NoOpSpan) span).getSpanId());
	}

	@Override
	public String extractParentId(Span span) {
		return String.format("%x", ((NoOpSpan) span).getParentId());
	}

	@Override
	public Class<? extends Span> getSupportedSpanType() {
		return NoOpSpan.class;
	}

	@Override
	public Class<? extends Tracer> getSupportedTracerType() {
		// TODO Auto-generated method stub
		return NoOpTracer.class;
	}
}
