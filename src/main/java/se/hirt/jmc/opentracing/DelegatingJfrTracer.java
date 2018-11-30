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
package se.hirt.jmc.opentracing;

import java.util.logging.Logger;

import io.opentracing.ScopeManager;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import se.hirt.jmc.opentracing.extractors.ExtractorRegistry;
import se.hirt.jmc.opentracing.noop.NoOpTracer;

/**
 * A tracer that records context information into the JDK Flight Recorder, making it possible to
 * correlate interesting findings in distributed traces with detailed information in the flight
 * recordings.
 * <p>
 * For scopes and spans the following will be recorded:
 * <ul>
 * <li>Trace Id</li>
 * <li>Span Id</li>
 * <li>Parent Id</li>
 * </ul>
 * 
 * @author Marcus Hirt
 */
public final class DelegatingJfrTracer implements Tracer {
	private final Tracer delegate;
	private final ContextExtractor extractor;
	private final ScopeManagerWrapper scopeManager;

	public DelegatingJfrTracer(Tracer delegate) {
		this.delegate = initialize(delegate);
		this.extractor = initializeExtractor(delegate);
		this.scopeManager = new ScopeManagerWrapper(delegate.scopeManager(), extractor);
	}

	private static ContextExtractor initializeExtractor(Tracer delegate) {
		ContextExtractor extractor = ExtractorRegistry.createNewRegistry()
				.getExtractorByTracerType(delegate.getClass());
		if (extractor != null) {
			return extractor;
		} else {
			Logger.getLogger(DelegatingJfrTracer.class.getName())
					.warning("No compatible context extractor found for tracer of type " + delegate.getClass().getName()
							+ ". The DelegatingJfrTracer will not work. Exiting process...");
			System.exit(4711);
		}
		return null;
	}

	private static Tracer initialize(Tracer delegate) {
		Logger.getLogger(DelegatingJfrTracer.class.getName())
				.info("Using DelegatingJfrTracer to capture contextual information into JFR.");

		if (delegate == null) {
			Logger.getLogger(DelegatingJfrTracer.class.getName()).info("No delegate set - will only log to JFR.");
		}
		return delegate == null ? new NoOpTracer() : delegate;
	}

	@Override
	public ScopeManager scopeManager() {
		return scopeManager;
	}

	@Override
	public Span activeSpan() {
		return scopeManager.active().span();
	}

	@Override
	public SpanBuilder buildSpan(String operationName) {
		return new SpanBuilderWrapper(this, delegate.buildSpan(operationName));
	}

	@Override
	public <C> void inject(SpanContext spanContext, Format<C> format, C carrier) {
		delegate.inject(spanContext, format, carrier);
	}

	@Override
	public <C> SpanContext extract(Format<C> format, C carrier) {
		return delegate.extract(format, carrier);
	}

	public ContextExtractor getContextExtractor() {
		return extractor;
	}

	public String toString(Span span) {
		return String.format("Trace id: %s, Span id: %s, Parent id: %s", extractor.extractTraceId(span),
				extractor.extractSpanId(span), extractor.extractParentId(span));
	}
}
