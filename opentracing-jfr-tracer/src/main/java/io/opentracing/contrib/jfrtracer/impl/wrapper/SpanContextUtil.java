package io.opentracing.contrib.jfrtracer.impl.wrapper;

import brave.opentracing.BraveSpanContext;
import io.jaegertracing.internal.JaegerSpanContext;
import io.opentracing.SpanContext;

/**
 * SpanContextUtil allows to extract traceId and spanId from tracers current implementation should
 * be simplified when OpenTracing API for Java version 0.32.0 is released
 */
//TODO: simplify when Jaeger and Brave supports 0.32.0
public final class SpanContextUtil {

	/**
	 * extract traceId from SpanContext
	 * 
	 * @param context
	 *            SpanContext
	 * @return traceId
	 */
	public static String getTraceIdBySpanContext(SpanContext context) {
		if (context instanceof JaegerSpanContext) {
			JaegerSpanContext jaegerSpanContext = (JaegerSpanContext) context;
			return jaegerSpanContext.getTraceId();
		} else if (context instanceof BraveSpanContext) {
			BraveSpanContext braveSpanContext = (BraveSpanContext) context;
			return braveSpanContext.unwrap() != null ? "" : braveSpanContext.unwrap().traceIdString();
		} else {
			return context.toTraceId();
		}
	}

    /**
     * extract spanId from SpanContext
     * @param context SpanContext
     * @return spanId
     */
	public static String getSpanIdBySpanContext(SpanContext context) {
		if (context instanceof JaegerSpanContext) {
			JaegerSpanContext jaegerSpanContext = (JaegerSpanContext) context;
			return String.valueOf(jaegerSpanContext.getSpanId());
		} else if (context instanceof BraveSpanContext) {
			BraveSpanContext braveSpanContext = (BraveSpanContext) context;
			return braveSpanContext.unwrap() == null ? "" : braveSpanContext.unwrap().spanIdString();
		} else {
			return context.toSpanId();
		}
	}
}
