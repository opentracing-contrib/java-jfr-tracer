package io.opentracing.contrib.jfrtracer.jfr;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.tag.Tag;
import jdk.jfr.Category;
import jdk.jfr.Description;
import jdk.jfr.FlightRecorder;
import jdk.jfr.Label;

import java.util.Map;

import static java.util.Objects.isNull;

@Category("OpenTracing")
@Label("OpenTracing JFR Event")
@Description("Open Tracing spans exposed as a JFR event")
public class JFRSpan extends jdk.jfr.Event implements Span {

	private static FlightRecorder jfr;

	private final Span span;

	@Label("Trace ID")
	@Description("Trace ID that will be the same for all spans that are part of the same trace")
	private String traceId;

	@Label("Span ID")
	@Description("Span ID that will be unique for every span")
	private String spanId;

	@Label("Parent Span ID")
	@Description("ID of the parent span. Null if root span")
	private final String parentSpanId;

	@Label("Operation Name")
	@Description("Operation name of the span")
	private String name;

	@Label("Start Thread")
	@Description("Thread starting the span")
	private final Thread startThread;

	@Label("Finish Thread")
	@Description("Thread finishing the span")
	private Thread finishThread;

	private JFRSpan(Span span, String name, String parentSpanId) {
		this.name = name;
		this.startThread = Thread.currentThread();
		this.span = span;
		this.spanId = span.context().toSpanId();
		this.traceId = span.context().toTraceId();
		this.parentSpanId = parentSpanId;
	}

	public String getTraceId() {
		return traceId;
	}

	public String getSpanId() {
		return spanId;
	}

	public String getParentSpanId() {
		return parentSpanId;
	}

	public String getName() {
		return name;
	}

	public Thread getStartThread() {
		return startThread;
	}

	public Thread getFinishThread() {
		return finishThread;
	}

	public Span unwrap() {
		return span;
	}

	@Override
	public SpanContext context() {
		return span.context();
	}

	@Override
	public Span setTag(String key, String value) {
		span.setTag(key, value);
		return this;
	}

	@Override
	public Span setTag(String key, boolean value) {
		span.setTag(key, value);
		return this;
	}

	@Override
	public Span setTag(String key, Number value) {
		span.setTag(key, value);
		return this;
	}

	@Override
	public <T> Span setTag(Tag<T> tag, T value) {
		span.setTag(tag, value);
		return this;
	}

	@Override
	public Span log(Map<String, ?> fields) {
		span.log(fields);
		return this;
	}

	@Override
	public Span log(long timestampMicroseconds, Map<String, ?> fields) {
		span.log(timestampMicroseconds, fields);
		return this;
	}

	@Override
	public Span log(String event) {
		span.log(event);
		return this;
	}

	@Override
	public Span log(long timestampMicroseconds, String event) {
		span.log(timestampMicroseconds, event);
		return this;
	}

	@Override
	public Span setBaggageItem(String key, String value) {
		span.setBaggageItem(key, value);
		return this;
	}

	@Override
	public String getBaggageItem(String key) {
		return span.getBaggageItem(key);
	}

	@Override
	public Span setOperationName(String operationName) {
		this.name = operationName;
		span.setOperationName(operationName);
		return this;
	}

	@Override
	public void finish() {
		finishJFR();
		span.finish();
	}

	@Override
	public void finish(long finishMicros) {
		finishJFR();
		span.finish(finishMicros);
	}

	void finishJFR() {
		if (shouldCommit()) {
			finishThread = Thread.currentThread();
			end();
			commit();
		}
	}

	static Span createJFRSpan(Tracer tracer, Span span, String operationName, String parentSpanId) {
		if (FlightRecorder.isAvailable() && FlightRecorder.isInitialized()) {

			// Avoid synchronization
			if (isNull(jfr)) {
				jfr = FlightRecorder.getFlightRecorder();
			}

			if (!jfr.getRecordings().isEmpty()) {
				JFRSpan jfrSpan = new JFRSpan(span, operationName, parentSpanId);
				jfrSpan.begin();
				return jfrSpan;
			}
		}
		return span;
	}
}
