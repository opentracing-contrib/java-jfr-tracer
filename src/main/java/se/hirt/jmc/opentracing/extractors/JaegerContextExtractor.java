package se.hirt.jmc.opentracing.extractors;

import io.opentracing.Span;
import io.opentracing.Tracer;
import se.hirt.jmc.opentracing.ContextExtractor;

public class JaegerContextExtractor implements ContextExtractor {
	@Override
	public String extractTraceId(Span span) {
		return String.format("%x", ((io.jaegertracing.Span) span).context().getTraceId());
	}

	@Override
	public String extractSpanId(Span span) {
		return String.format("%x", ((io.jaegertracing.Span) span).context().getSpanId());
	}

	@Override
	public String extractParentId(Span span) {
		return String.format("%x", ((io.jaegertracing.Span) span).context().getParentId());
	}

	@Override
	public Class<? extends Span> getSupportedSpanType() {
		return io.jaegertracing.Span.class;
	}

	@Override
	public Class<? extends Tracer> getSupportedTracerType() {
		return io.jaegertracing.Tracer.class;
	}
}
