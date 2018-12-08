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
import io.opentracing.ScopeManager;
import io.opentracing.Span;

/**
 * Wrapper for {@link ScopeManager}.
 */
final class ScopeManagerWrapper implements ScopeManager {
	private final ScopeManager delegate;
	private final ThreadLocal<ScopeWrapper> activeScope = new ThreadLocal<>();
	
	ScopeManagerWrapper(ScopeManager delegate) {
		this.delegate = delegate;
	}

	@SuppressWarnings("deprecation")
	@Override
	public Scope activate(Span span, boolean finishSpanOnClose) {
		ScopeWrapper wrapper;
		if (!(span instanceof SpanWrapper)) {
			SpanWrapper spanWrapper = new SpanWrapper(span);
			wrapper = new ScopeWrapper(spanWrapper, delegate.activate(span, finishSpanOnClose), finishSpanOnClose); 
		} else {
			SpanWrapper spanWrapper = (SpanWrapper) span;
			wrapper = new ScopeWrapper(spanWrapper, delegate.activate(spanWrapper.getDelegate(), finishSpanOnClose), finishSpanOnClose);
		}
		activeScope.set(wrapper);
		return wrapper;
	}

	@Override
	public Scope active() {
		return activeScope.get();
	}

	@Override
	public Scope activate(Span arg) {
		return activate(arg, false);
	}

	@Override
	public Span activeSpan() {
		return delegate.activeSpan();
	}
}
