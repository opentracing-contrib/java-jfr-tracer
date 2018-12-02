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

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer.SpanBuilder;

/**
 * Wrapper for {@link SpanBuilder}.
 */
final class SpanBuilderWrapper implements SpanBuilder {
	private final DelegatingJfrTracer owner;
	private final SpanBuilder delegate;
	private final String operationName;

	private SpanWrapper spanWrapper;

	SpanBuilderWrapper(DelegatingJfrTracer owner, String operationName, SpanBuilder delegate) {
		this.owner = owner;
		this.delegate = delegate;
		this.operationName = operationName;
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
			spanWrapper = new SpanWrapper(delegate.startManual(), operationName, owner.getContextExtractor());
		}
		return spanWrapper;
	}

	@Override
	public Span start() {
		if (spanWrapper == null) {
			spanWrapper = new SpanWrapper(delegate.start(), operationName, owner.getContextExtractor());
			spanWrapper.start();
		}
		return spanWrapper;
	}

}
