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
package io.opentracing.contrib.jfrtracer.impl.wrapper;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer.SpanBuilder;
import io.opentracing.tag.Tag;

/**
 * Wrapper for {@link SpanBuilder}.
 */
final class SpanBuilderWrapper implements SpanBuilder {

	private final TracerWrapper owner;
	private final SpanBuilder delegate;
	private final String operationName;
	// Not sure how likely it is that these builders get passed around,
	// but assumption is the mother of all...
	private volatile String parentId;

	SpanBuilderWrapper(TracerWrapper owner, String operationName, SpanBuilder delegate) {
		this.owner = owner;
		this.delegate = delegate;
		this.operationName = operationName;
	}

	@Override
	public SpanBuilder asChildOf(SpanContext parent) {
		delegate.asChildOf(parent);
		if (parent == null) {
			return this;
		}
		parentId = parent.toSpanId();
		return this;
	}

	@Override
	public SpanBuilder asChildOf(Span parent) {
		delegate.asChildOf(parent);
		if (parent == null) {
			return this;
		}
		parentId = parent.context().toSpanId();
		return this;
	}

	@Override
	public SpanBuilder addReference(String referenceType, SpanContext referencedContext) {
		delegate.addReference(referenceType, referencedContext);
		if (referencedContext == null) {
			return this;
		}
		parentId = referencedContext.toSpanId();
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
	public <T> SpanBuilder withTag(Tag<T> key, T value) {
		delegate.withTag(key, value);
		return this;
	}

	@Override
	public SpanBuilder withStartTimestamp(long microseconds) {
		delegate.withStartTimestamp(microseconds);
		return this;
	}

	@Override
	@Deprecated
	public Scope startActive(boolean finishSpanOnClose) {
		return owner.scopeManager().activate(start(), finishSpanOnClose);
	}

	@Override
	@Deprecated
	public Span startManual() {
		return new SpanWrapper(getParentSpanId(), delegate.startManual(), operationName);
	}

	@Override
	public Span start() {
		SpanWrapper spanWrapper = new SpanWrapper(getParentSpanId(), delegate.start(), operationName);
		spanWrapper.start();
		return spanWrapper;
	}

	private String getParentSpanId() {
		Span activeSpan = owner.scopeManager().activeSpan();
		return parentId != null ? parentId : activeSpan != null ? activeSpan.context().toSpanId() : null;
	}

	/**
	 * hook for unit test
	 */
	String parentId() {
		return parentId;
	}
}
