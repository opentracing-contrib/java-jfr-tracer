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

import io.opentracing.ScopeManager;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import se.hirt.jmc.opentracing.noop.NoOpTracer;

/**
 * 
 * @author Marcus Hirt
 */
public final class DelegatingJfrTracer implements Tracer {
	private final Tracer delegate;

	public DelegatingJfrTracer(Tracer delegate) {
		this.delegate = delegate == null ? new NoOpTracer() : delegate;
	}

	@Override
	public ScopeManager scopeManager() {
		return delegate.scopeManager();
	}

	@Override
	public Span activeSpan() {
		// Will probably want to keep track of this separately...
		return new SpanWrapper(delegate.activeSpan());
	}

	@Override
	public SpanBuilder buildSpan(String operationName) {
		return new SpanBuilderWrapper(delegate.buildSpan(operationName));
	}

	@Override
	public <C> void inject(SpanContext spanContext, Format<C> format, C carrier) {
		delegate.inject(spanContext, format, carrier);
	}

	@Override
	public <C> SpanContext extract(Format<C> format, C carrier) {
		return delegate.extract(format, carrier);
	}
}
