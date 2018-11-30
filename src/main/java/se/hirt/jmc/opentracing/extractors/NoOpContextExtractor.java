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
		return NoOpTracer.class;
	}
}
