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

import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;

/**
 * Wrapper for {@link ScopeManager}.
 * 
 * @author Marcus Hirt
 */
final class ScopeManagerWrapper implements ScopeManager {
	private final ScopeManager delegate;
	private final ContextExtractor extractor;
	private final ThreadLocal<ScopeWrapper> activeScope = new ThreadLocal<>();

	ScopeManagerWrapper(ScopeManager delegate, ContextExtractor extractor) {
		this.delegate = delegate;
		this.extractor = extractor;
	}

	@Override
	public Scope activate(Span span, boolean finishSpanOnClose) {
		ScopeWrapper wrapper;
		if (!(span instanceof SpanWrapper)) {
			SpanWrapper spanWrapper = new SpanWrapper(span, extractor);
			wrapper = new ScopeWrapper(spanWrapper, delegate.activate(span, finishSpanOnClose), extractor); 
		} else {
			SpanWrapper spanWrapper = (SpanWrapper) span;
			wrapper = new ScopeWrapper(spanWrapper, delegate.activate(spanWrapper.getDelegate(), finishSpanOnClose), extractor);
		}
		activeScope.set(wrapper);
		return wrapper;
	}

	@Override
	public Scope active() {
		return activeScope.get();
	}
}
