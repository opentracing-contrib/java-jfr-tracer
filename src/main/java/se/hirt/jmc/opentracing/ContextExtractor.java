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
package se.hirt.jmc.opentracing;

import io.opentracing.Span;
import io.opentracing.Tracer;

/**
 * Since the key context information is not readily available from the OpenTracing API, we sadly
 * need to have vendor specific implementations provided for the various tracers out there.
 * 
 * @author Marcus Hirt
 */
public interface ContextExtractor {
	/**
	 * Extracts the operation name from the span.
	 */
	String extractOperationName(Span span);
	
	/**
	 * Extracts the vendor specific trace id from the span.
	 */
	String extractTraceId(Span span);

	/**
	 * Extracts the vendor specific span id from the span.
	 */
	String extractSpanId(Span span);

	/**
	 * Extracts the vendor specific parent id from the span.
	 */
	String extractParentId(Span span);

	/**
	 * @return the span class that this context extractor supports.
	 */
	Class<? extends Span> getSupportedSpanType();

	/**
	 * @return the tracer class that this context extractor supports.
	 */
	Class<? extends Tracer> getSupportedTracerType();
}
