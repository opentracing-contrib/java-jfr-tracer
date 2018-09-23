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

import java.util.logging.Logger;

import io.opentracing.ScopeManager;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import se.hirt.jmc.opentracing.extractors.ExtractorRegistry;
import se.hirt.jmc.opentracing.noop.NoOpTracer;

/**
 * @author Marcus Hirt
 */
public final class DelegatingJfrTracer implements Tracer {
	private final Tracer delegate;
	private final ContextExtractor extractor;

	public DelegatingJfrTracer(Tracer delegate) {
		this.delegate = initialize(delegate);
		this.extractor = initializeExtractor(delegate);
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
		return new ScopeManagerWrapper(delegate.scopeManager(), getContextExtractor());
	}

	@Override
	public Span activeSpan() {
		// Will probably want to keep track of this separately...
		return new SpanWrapper(delegate.activeSpan());
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
