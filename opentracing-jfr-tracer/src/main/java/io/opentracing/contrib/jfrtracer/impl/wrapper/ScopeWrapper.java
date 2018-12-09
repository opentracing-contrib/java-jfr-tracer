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
import io.opentracing.contrib.jfrtracer.impl.jfr.JfrEmitter;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Wrapper for {@link Scope}.
 */
final class ScopeWrapper implements Scope {

	private static final Logger LOG = Logger.getLogger(ScopeWrapper.class.getName());

	private final ScopeManagerWrapper scopeManagerWrapper;
	private final Scope delegate;
	private final JfrEmitter emitter;
	private final SpanWrapper spanWrapper;
	private final boolean finishSpanOnClose;
	private final ScopeWrapper parentScope;

	ScopeWrapper(ScopeManagerWrapper scopeManagerWrapper, SpanWrapper spanWrapper, Scope delegate, boolean finishSpanOnClose) {
		this.scopeManagerWrapper = scopeManagerWrapper;
		this.parentScope = (ScopeWrapper) scopeManagerWrapper.active();
		this.spanWrapper = spanWrapper;
		this.delegate = delegate;
		emitter = SpanWrapper.EMITTER_FACTORY.createScopeEmitter(spanWrapper);
		emitter.start(spanWrapper.getParentId(), spanWrapper.getOperationName());
		this.finishSpanOnClose = finishSpanOnClose;
	}

	@Override
	public void close() {
		delegate.close();
		closeEmitter();
		if (finishSpanOnClose) {
			spanWrapper.closeEmitter();
		}
		scopeManagerWrapper.setActive(parentScope);
	}

	@Override
	@Deprecated
	public Span span() {
		return spanWrapper;
	}

	private void closeEmitter() {
		try {
			emitter.close();
		} catch (Exception ex) {
			LOG.log(Level.SEVERE, "Error closing JFR Span", ex);
		}
	}
}
