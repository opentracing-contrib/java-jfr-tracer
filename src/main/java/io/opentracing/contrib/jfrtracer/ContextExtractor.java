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
package io.opentracing.contrib.jfrtracer;

import io.opentracing.Span;
import io.opentracing.Tracer;

/**
 * Since the key context information is not readily available from the OpenTracing API, we sadly
 * need to have vendor specific implementations provided for the various tracers out there.
 */
public interface ContextExtractor {

	/**
	 * Extracts the vendor specific trace id from the span. If your tracer does not have the concept
	 * of a trace id, use the symbol with the closest meaning that can be used for a user to find
	 * specific events.
	 * 
	 * @param span
	 *            the Span to extract the vendor specific trace id from.
	 * @return the trace id derived from the span, or null if something similar to a trace id cannot
	 *         be derived from a span for this tracer.
	 */
	String extractTraceId(Span span);

	/**
	 * Extracts the vendor specific span id from the span. If your tracer does not have the concept
	 * of a span id, use the symbol with the closest meaning that can be used for a user to find
	 * specific events.
	 * 
	 * @param span
	 *            the Span to extract the vendor specific span id from.
	 * @return the span id derived from the span, or null if something similar to a span id cannot
	 *         be derived from a span for this tracer.
	 */
	String extractSpanId(Span span);

	/**
	 * Extracts the vendor specific parent id from the span. If your tracer does not have the
	 * concept of a parent id, use the symbol with the closest meaning that can be used for a user
	 * to find specific events.
	 * 
	 * @param span
	 *            the Span to extract the vendor specific parent id from.
	 * @return the parent id derived from the span, or null if something similar to a parent id
	 *         cannot be derived from a span for this tracer.
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
