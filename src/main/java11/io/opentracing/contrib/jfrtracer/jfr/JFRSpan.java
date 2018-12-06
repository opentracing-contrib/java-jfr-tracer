package io.opentracing.contrib.jfrtracer.jfr;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import jdk.jfr.Category;
import jdk.jfr.Description;
import jdk.jfr.FlightRecorder;
import jdk.jfr.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import static java.util.Objects.isNull;

@Category("OpenTracing")
@Label("OpenTracing JFR Event")
@Description("Open Tracing spans exposed as a JFR event")
public class JFRSpan extends jdk.jfr.Event implements Span, TextMap {

	private final static Logger LOG = LoggerFactory.getLogger(JFRSpan.class);
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
	private String parentSpanId;

	@Label("Operation Name")
	@Description("Operation name of the span")
	private String name;

	@Label("Start Thread")
	@Description("Thread starting the span")
	private final Thread startThread;

	@Label("Finish Thread")
	@Description("Thread finishing the span")
	private Thread finishThread;

	private JFRSpan(Span span, String name) {
		this.name = name;
		this.startThread = Thread.currentThread();
		this.span = span;
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
	public Iterator<Entry<String, String>> iterator() {
		return Collections.emptyIterator();
	}

	/**
	 * Supports injection with MockTracer, uber-trace-id, and B3 headers
	 *
	 * @param key
	 * @param value
	 */
	@Override
	public void put(String key, String value) {
		switch (key) {
			case "X-B3-TraceId":
			case "traceid":
				this.traceId = value;
				break;
			case "X-B3-SpanId":
			case "spanid":
				this.spanId = value;
				break;
			case "X-B3-ParentSpanId":
				this.parentSpanId = value;
				break;
			case "X-B3-Sampled":
				break;
			case "uber-trace-id":
				String[] values = value.split(":");
				this.traceId = values[0];
				this.spanId = values[1];
				this.parentSpanId = values[2].equals("0") ? null : values[2];
				break;
			default:
				LOG.warn("Unsupported injection key: " + key);
		}
	}

	@Override
	public SpanContext context() {
		return span.context();
	}

	@Override
	public Span setTag(String key, String value) {
		return span.setTag(key, value);
	}

	@Override
	public Span setTag(String key, boolean value) {
		return span.setTag(key, value);
	}

	@Override
	public Span setTag(String key, Number value) {
		return span.setTag(key, value);
	}

	@Override
	public Span log(Map<String, ?> fields) {
		return span.log(fields);
	}

	@Override
	public Span log(long timestampMicroseconds, Map<String, ?> fields) {
		return span.log(timestampMicroseconds, fields);
	}

	@Override
	public Span log(String event) {
		return span.log(event);
	}

	@Override
	public Span log(long timestampMicroseconds, String event) {
		return span.log(timestampMicroseconds, event);
	}

	@Override
	public Span setBaggageItem(String key, String value) {
		return span.setBaggageItem(key, value);
	}

	@Override
	public String getBaggageItem(String key) {
		return span.getBaggageItem(key);
	}

	@Override
	public Span setOperationName(String operationName) {
		this.name = operationName;
		return span.setOperationName(operationName);
	}

	@Override
	public void finish() {
		finishJFR();
		span.finish();
	}

	@Override
	public void finish(long finishMicros) {
		finishJFR();
		span.finish();
	}

	void finishJFR() {
		if (shouldCommit()) {
			this.finishThread = Thread.currentThread();
			end();
			commit();
		}
	}

	static Span createJFRSpan(Tracer tracer, Span span, String operationName) {
		if (FlightRecorder.isAvailable() && FlightRecorder.isInitialized()) {

			// Avoid synchronization
			if (isNull(jfr)) {
				jfr = FlightRecorder.getFlightRecorder();
			}

			if (!jfr.getRecordings().isEmpty()) {
				JFRSpan jfrSpan = new JFRSpan(span, operationName);
				tracer.inject(span.context(), Format.Builtin.TEXT_MAP, jfrSpan);
				jfrSpan.begin();
				return jfrSpan;
			}
		}
		return span;
	}
}
