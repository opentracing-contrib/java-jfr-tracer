package se.hirt.jmc.opentracing;

import io.opentracing.Span;
import io.opentracing.Tracer;

/**
 * Since the key context information is not readily available from the OpenTracing API, we sadly
 * need to have vendor specific implementations provided for the various tracers out there.
 * 
 * @author Marcus Hirt
 */
public interface ContextExtractor {
	/**
	 * Extracts the vendor specific trace id from the span.
	 */
	String extractTraceId(Span span);

	/**
	 * Extracts the vendor specific span id from the span.
	 */
	String extractSpanId(Span span);

	/**
	 * Extracts the vendor specific parent id from the span.
	 */
	String extractParentId(Span span);

	/**
	 * @return the span class that this context extractor supports.
	 */
	Class<? extends Span> getSupportedSpanType();

	/**
	 * @return the tracer class that this context extractor supports.
	 */
	Class<? extends Tracer> getSupportedTracerType();
}
