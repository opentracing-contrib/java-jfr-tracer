package io.opentracing.contrib.jfrtracer.jfr;

import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tag;

import static io.opentracing.contrib.jfrtracer.jfr.JFRSpan.createJFRSpan;

public class JFRTracerImpl implements Tracer {

	private final Tracer tracer;
	private final JFRScopeManager scopeManager;

	public JFRTracerImpl(Tracer tracer) {
		this.tracer = tracer;
		this.scopeManager = new JFRScopeManager(tracer);
	}

	@Override
	public ScopeManager scopeManager() {
		return scopeManager;
	}

	@Override
	public Span activeSpan() {
		return tracer.activeSpan();
	}

	@Override
	public Scope activateSpan(Span span) {
		return scopeManager.activate(span, true);
	}

	@Override
	public SpanBuilder buildSpan(String operationName) {
		SpanBuilder spanBuilder = tracer.buildSpan(operationName);
		return new JFRSpanBuilder(operationName, spanBuilder);
	}

	@Override
	public <C> void inject(SpanContext spanContext, Format<C> format, C carrier) {
		tracer.inject(spanContext, format, carrier);
	}

	@Override
	public <C> SpanContext extract(Format<C> format, C carrier) {
		return tracer.extract(format, carrier);
	}

	private class JFRSpanBuilder implements SpanBuilder {

		private final SpanBuilder spanBuilder;
		private final String operationName;
		private String parentSpanId;

		JFRSpanBuilder(String operationName, SpanBuilder spanBuilder) {
			this.operationName = operationName;
			this.spanBuilder = spanBuilder;
		}

		@Override
		public SpanBuilder asChildOf(SpanContext parent) {
			this.parentSpanId = parent.toSpanId();
			spanBuilder.asChildOf(parent);
			return this;
		}

		@Override
		public SpanBuilder asChildOf(Span parent) {
			this.parentSpanId = parent.context().toSpanId();
			spanBuilder.asChildOf(parent);
			return this;
		}

		@Override
		public SpanBuilder addReference(String referenceType, SpanContext referencedContext) {
			spanBuilder.addReference(referenceType, referencedContext);
			return this;
		}

		@Override
		public SpanBuilder ignoreActiveSpan() {
			spanBuilder.ignoreActiveSpan();
			return this;
		}

		@Override
		public SpanBuilder withTag(String key, String value) {
			spanBuilder.withTag(key, value);
			return this;
		}

		@Override
		public SpanBuilder withTag(String key, boolean value) {
			spanBuilder.withTag(key, value);
			return this;
		}

		@Override
		public SpanBuilder withTag(String key, Number value) {
			spanBuilder.withTag(key, value);
			return this;
		}

		@Override
		public <T> SpanBuilder withTag(Tag<T> tag, T value) {
			spanBuilder.withTag(tag, value);
			return this;
		}

		@Override
		public SpanBuilder withStartTimestamp(long microseconds) {
			spanBuilder.withStartTimestamp(microseconds);
			return this;
		}

		@Override
		@Deprecated
		public Scope startActive(boolean finishSpanOnClose) {
			return scopeManager.activate(start(), finishSpanOnClose);
		}

		@Override
		@Deprecated
		public Span startManual() {
			Span span = spanBuilder.startManual();
			return createJFRSpan(tracer, span, operationName, getParentSpanId());
		}

		@Override
		public Span start() {
			Span span = spanBuilder.start();
			return createJFRSpan(tracer, span, operationName, getParentSpanId());
		}

		private String getParentSpanId() {
			Span activeSpan = tracer.activeSpan();
			return parentSpanId != null ? parentSpanId
					: activeSpan != null ? activeSpan.context().toSpanId()
							: null;
		}
	}
}
