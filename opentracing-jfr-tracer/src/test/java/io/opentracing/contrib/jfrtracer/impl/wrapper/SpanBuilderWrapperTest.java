package io.opentracing.contrib.jfrtracer.impl.wrapper;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer.SpanBuilder;
import io.opentracing.mock.MockTracer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class SpanBuilderWrapperTest {

    @Test
    void asChildOfOnParentSpanContext() {
        MockTracer delegateTracer = new MockTracer();
        Span parentSpan = delegateTracer.buildSpan("parentSpan").start();
        TracerWrapper tracerWrapper = new TracerWrapper(delegateTracer);
        String operationName = "asChildOfOnParentSpanContext";
        SpanBuilderWrapper spanBuilderWrapper = new SpanBuilderWrapper(tracerWrapper, operationName,
                delegateTracer.buildSpan(operationName));

        SpanBuilder spanBuilder = spanBuilderWrapper.asChildOf(parentSpan.context());
        assertSame(spanBuilderWrapper, spanBuilder);
        assertNotNull(spanBuilderWrapper.parentId());
        assertEquals(parentSpan.context().toSpanId(), spanBuilderWrapper.parentId());
    }

    @Test
    void asChildOfOnParentSpanContextNull() {
        MockTracer delegateTracer = new MockTracer();
        TracerWrapper tracerWrapper = new TracerWrapper(delegateTracer);
        String operationName = "asChildOfOnParentSpanContextNull";
        SpanBuilderWrapper spanBuilderWrapper = new SpanBuilderWrapper(tracerWrapper, operationName,
                delegateTracer.buildSpan(operationName));

        SpanBuilder spanBuilder = spanBuilderWrapper.asChildOf((SpanContext) null);
        assertSame(spanBuilderWrapper, spanBuilder);
        assertNull(spanBuilderWrapper.parentId());
    }

    @Test
    void asChildOfOnParentSpan() {
        MockTracer delegateTracer = new MockTracer();
        Span parentSpan = delegateTracer.buildSpan("parentSpan").start();
        TracerWrapper tracerWrapper = new TracerWrapper(delegateTracer);
        String operationName = "asChildOfOnParentSpan";
        SpanBuilderWrapper spanBuilderWrapper = new SpanBuilderWrapper(tracerWrapper, operationName,
                delegateTracer.buildSpan(operationName));

        SpanBuilder spanBuilder = spanBuilderWrapper.asChildOf(parentSpan);
        assertSame(spanBuilderWrapper, spanBuilder);
        assertNotNull(spanBuilderWrapper.parentId());
        assertEquals(parentSpan.context().toSpanId(), spanBuilderWrapper.parentId());
    }

    @Test
    void asChildOfOnParentSpanNull() {
        MockTracer delegateTracer = new MockTracer();
        TracerWrapper tracerWrapper = new TracerWrapper(delegateTracer);
        String operationName = "asChildOfOnParentSpanNull";
        SpanBuilderWrapper spanBuilderWrapper = new SpanBuilderWrapper(tracerWrapper, operationName,
                delegateTracer.buildSpan(operationName));

        SpanBuilder spanBuilder = spanBuilderWrapper.asChildOf((Span) null);
        assertSame(spanBuilderWrapper, spanBuilder);
        assertNull(spanBuilderWrapper.parentId());
    }
}