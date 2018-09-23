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
package se.hirt.jmc.opentracing.noop;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;

/**
 * A do nothing tracer that is used if there is no delegating tracer to delegate to.
 * 
 * @author Marcus Hirt
 */
public final class NoOpTracer implements Tracer {
	private static final Random RND = new Random();
	private final NoOpScopeManager scopeManager = new NoOpScopeManager();
	private volatile Scope activeScope;

	private class NoOpScopeManager implements ScopeManager {
		private volatile Span activeSpan;

		@Override
		public Scope activate(Span span, boolean finishSpanOnClose) {
			activeSpan = span;
			return new NoOpScope(span, finishSpanOnClose);
		}

		@Override
		public Scope active() {
			return activeScope;
		}

		public Span getActiveSpan() {
			return activeSpan;
		}
	}

	private class NoOpScope implements Scope {
		private final Span span;
		private final boolean finishSpanOnClose;

		public NoOpScope(Span span, boolean finishSpanOnClose) {
			this.span = span;
			this.finishSpanOnClose = finishSpanOnClose;
		}

		@Override
		public void close() {
			if (finishSpanOnClose) {
				span.finish();
			}
		}

		@Override
		public Span span() {
			return span;
		}
	}

	public class NoOpSpan implements Span {
		private String operationName;
		private final long traceId;
		private final long spanId;
		private final long parentId;

		private NoOpSpan(String operationName, long traceId, long spanId, long parentId) {
			this.operationName = operationName;
			this.traceId = traceId;
			this.spanId = spanId;
			this.parentId = parentId;
		}

		@Override
		public SpanContext context() {
			return new NoOpSpanContext();
		}

		@Override
		public Span setTag(String key, String value) {
			return this;
		}

		@Override
		public Span setTag(String key, boolean value) {
			return this;
		}

		@Override
		public Span setTag(String key, Number value) {
			return this;
		}

		@Override
		public Span log(Map<String, ?> fields) {
			return this;
		}

		@Override
		public Span log(long timestampMicroseconds, Map<String, ?> fields) {
			return this;
		}

		@Override
		public Span log(String event) {
			return this;
		}

		@Override
		public Span log(long timestampMicroseconds, String event) {
			return this;
		}

		@Override
		public Span setBaggageItem(String key, String value) {
			return this;
		}

		@Override
		public String getBaggageItem(String key) {
			return null;
		}

		@Override
		public Span setOperationName(String operationName) {
			this.operationName = operationName;
			return this;
		}

		@Override
		public void finish() {
		}

		@Override
		public void finish(long finishMicros) {
		}

		public String toString() {
			return operationName;
		}

		public long getTraceId() {
			return traceId;
		}

		public long getSpanId() {
			return spanId;
		}

		public long getParentId() {
			return parentId;
		}
	}

	public class NoOpSpanBuilder implements SpanBuilder {
		private final String operationName;
		private boolean ignoreActiveSpan;

		public NoOpSpanBuilder(String operationName) {
			this.operationName = operationName;
		}

		@Override
		public SpanBuilder asChildOf(SpanContext parent) {
			return this;
		}

		@Override
		public SpanBuilder asChildOf(Span parent) {
			return this;
		}

		@Override
		public SpanBuilder addReference(String referenceType, SpanContext referencedContext) {
			return this;
		}

		@Override
		public SpanBuilder ignoreActiveSpan() {
			ignoreActiveSpan = true;
			return this;
		}

		@Override
		public SpanBuilder withTag(String key, String value) {
			return this;
		}

		@Override
		public SpanBuilder withTag(String key, boolean value) {
			return this;
		}

		@Override
		public SpanBuilder withTag(String key, Number value) {
			return this;
		}

		@Override
		public SpanBuilder withStartTimestamp(long microseconds) {
			return this;
		}

		@Override
		public Scope startActive(boolean finishSpanOnClose) {
			return scopeManager.activate(start(), finishSpanOnClose);
		}

		@Override
		public Span startManual() {
			return new NoOpSpan(operationName, nextLong(), nextLong(), nextLong());
		}

		private long nextLong() {
			return Math.abs(RND.nextLong());
		}

		@Override
		public Span start() {
			return new NoOpSpan(operationName, nextLong(), nextLong(), nextLong());
		}

		public String toString() {
			return operationName + " ignoreActiveSpan=" + ignoreActiveSpan;
		}
	}

	public final class NoOpSpanContext implements SpanContext {
		@Override
		public Iterable<Entry<String, String>> baggageItems() {
			Map<String, String> map = Collections.emptyMap();
			return map.entrySet();
		}
	}

	@Override
	public ScopeManager scopeManager() {
		return scopeManager;
	}

	@Override
	public Span activeSpan() {
		return scopeManager.getActiveSpan();
	}

	@Override
	public SpanBuilder buildSpan(String operationName) {
		return new NoOpSpanBuilder(operationName);
	}

	@Override
	public <C> void inject(SpanContext spanContext, Format<C> format, C carrier) {
	}

	@Override
	public <C> SpanContext extract(Format<C> format, C carrier) {
		return new NoOpSpanContext();
	}
}
