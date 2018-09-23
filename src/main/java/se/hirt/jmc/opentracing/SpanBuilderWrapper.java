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

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer.SpanBuilder;

/**
 * Wrapper for {@link SpanBuilder}.
 * 
 * @author Marcus Hirt
 */
final class SpanBuilderWrapper implements SpanBuilder {
	private final DelegatingJfrTracer owner;
	private final SpanBuilder delegate;
	private SpanWrapper spanWrapper;

	SpanBuilderWrapper(DelegatingJfrTracer owner, SpanBuilder delegate) {
		this.owner = owner;
		this.delegate = delegate;
	}

	@Override
	public SpanBuilder asChildOf(SpanContext parent) {
		delegate.asChildOf(parent);
		return this;
	}

	@Override
	public SpanBuilder asChildOf(Span parent) {
		delegate.asChildOf(parent);
		return this;
	}

	@Override
	public SpanBuilder addReference(String referenceType, SpanContext referencedContext) {
		delegate.addReference(referenceType, referencedContext);
		return this;
	}

	@Override
	public SpanBuilder ignoreActiveSpan() {
		delegate.ignoreActiveSpan();
		return this;
	}

	@Override
	public SpanBuilder withTag(String key, String value) {
		delegate.withTag(key, value);
		return this;
	}

	@Override
	public SpanBuilder withTag(String key, boolean value) {
		delegate.withTag(key, value);
		return this;
	}

	@Override
	public SpanBuilder withTag(String key, Number value) {
		delegate.withTag(key, value);
		return this;
	}

	@Override
	public SpanBuilder withStartTimestamp(long microseconds) {
		delegate.withStartTimestamp(microseconds);
		return this;
	}

	@Override
	public Scope startActive(boolean finishSpanOnClose) {
		return owner.scopeManager().activate(start(), finishSpanOnClose);
	}

	@Override
	@Deprecated
	public Span startManual() {
		if (spanWrapper == null) {
			spanWrapper = new SpanWrapper(delegate.startManual(), owner.getContextExtractor());
		}
		return spanWrapper;
	}

	@Override
	public Span start() {
		if (spanWrapper == null) {
			spanWrapper = new SpanWrapper(delegate.start(), owner.getContextExtractor());
			spanWrapper.start();
		}
		return spanWrapper;
	}

}
