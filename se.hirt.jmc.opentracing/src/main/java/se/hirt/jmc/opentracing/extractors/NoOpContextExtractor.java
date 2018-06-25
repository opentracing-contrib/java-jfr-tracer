package se.hirt.jmc.opentracing.extractors;

import io.opentracing.Span;
import se.hirt.jmc.opentracing.ContextExtractor;
import se.hirt.jmc.opentracing.noop.NoOpTracer.NoOpSpan;

public class NoOpContextExtractor implements ContextExtractor {
	@Override
	public String extractTraceId(Span span) {
		return String.format("%x", ((NoOpSpan) span).getTraceId());
	}

	@Override
	public String extractSpanId(Span span) {
		return String.format("%x", ((NoOpSpan) span).getSpanId());
	}

	@Override
	public String extractParentId(Span span) {
		return String.format("%x", ((NoOpSpan) span).getParentId());
	}

	@Override
	public Class<? extends Span> getSupportedType() {
		return NoOpSpan.class;
	}

}
