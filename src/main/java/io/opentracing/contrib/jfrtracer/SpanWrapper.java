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

import java.util.Map;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.contrib.jfrtracer.jfr.JfrEmitter;
import io.opentracing.contrib.jfrtracer.jfr.JfrEmitterFactory;

/**
 * Wrapper for {@link Span}.
 */
final class SpanWrapper implements Span {
	static JfrEmitterFactory EMITTER_FACTORY = new JfrEmitterFactory();

	private final Span delegate;
	private final JfrEmitter spanEmitter;
	// If we don't want to support updates of the operation name, this could be final too...
	private volatile String operationName;

	SpanWrapper(Span delegate) {
		this.delegate = delegate;
		spanEmitter = EMITTER_FACTORY.createSpanEmitter(delegate);
	}

	public SpanWrapper(Span delegate, String operationName) {
		this(delegate);
		this.operationName = operationName;
	}

	@Override
	public SpanContext context() {
		return delegate.context();
	}

	@Override
	public Span setTag(String key, String value) {
		delegate.setTag(key, value);
		return this;
	}

	@Override
	public Span setTag(String key, boolean value) {
		delegate.setTag(key, value);
		return this;
	}

	@Override
	public Span setTag(String key, Number value) {
		delegate.setTag(key, value);
		return this;
	}

	@Override
	public Span log(Map<String, ?> fields) {
		delegate.log(fields);
		return this;
	}

	@Override
	public Span log(long timestampMicroseconds, Map<String, ?> fields) {
		delegate.log(timestampMicroseconds, fields);
		return this;
	}

	@Override
	public Span log(String event) {
		delegate.log(event);
		return this;
	}

	@Override
	public Span log(long timestampMicroseconds, String event) {
		delegate.log(timestampMicroseconds, event);
		return this;
	}

	@Override
	public Span setBaggageItem(String key, String value) {
		delegate.setBaggageItem(key, value);
		return this;
	}

	@Override
	public String getBaggageItem(String key) {
		return delegate.getBaggageItem(key);
	}

	@Override
	public Span setOperationName(String operationName) {
		this.operationName = operationName;
		delegate.setOperationName(operationName);
		return this;
	}

	@Override
	public void finish() {
		delegate.finish();
		try {
			spanEmitter.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void finish(long finishMicros) {
		delegate.finish(finishMicros);
		try {
			spanEmitter.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Span getDelegate() {
		return delegate;
	}

	public void start() {
		spanEmitter.start(operationName);
	}

	public String getOperationName() {
		return operationName;
	}
}
