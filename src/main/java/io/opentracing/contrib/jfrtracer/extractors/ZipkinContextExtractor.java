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
package io.opentracing.contrib.jfrtracer.extractors;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.contrib.jfrtracer.ContextExtractor;
import brave.opentracing.BraveSpan;
import brave.opentracing.BraveTracer;

/**
 * Extractor for the Open Zipkin/Brave tracer.
 * 
 * @see ContextExtractor
 */
public class ZipkinContextExtractor implements ContextExtractor {

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
