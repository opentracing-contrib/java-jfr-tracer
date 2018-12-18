package io.opentracing.contrib.jfrtracer.impl.wrapper;


import io.jaegertracing.internal.JaegerSpanContext;
import io.opentracing.SpanContext;

public final class SpanContextUtil {

    public static String getTraceIdBySpanContext(SpanContext context){
        if(context instanceof JaegerSpanContext){
            JaegerSpanContext jaegerSpanContext = (JaegerSpanContext) context;
            return jaegerSpanContext.getTraceId();
        } else {
            return context.toTraceId();
        }
    }

    public static String getSpanIdBySpanContext(SpanContext context){
        if(context instanceof JaegerSpanContext){
            JaegerSpanContext jaegerSpanContext = (JaegerSpanContext) context;
             return String.valueOf(jaegerSpanContext.getSpanId());
        } else {
            return context.toSpanId();
        }
    }
}
