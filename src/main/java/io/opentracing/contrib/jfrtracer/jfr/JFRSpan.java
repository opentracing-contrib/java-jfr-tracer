package io.opentracing.contrib.jfrtracer.jfr;

import com.oracle.jrockit.jfr.ContentType;
import com.oracle.jrockit.jfr.EventDefinition;
import com.oracle.jrockit.jfr.FlightRecorder;
import com.oracle.jrockit.jfr.InvalidEventDefinitionException;
import com.oracle.jrockit.jfr.InvalidValueException;
import com.oracle.jrockit.jfr.Producer;
import com.oracle.jrockit.jfr.TimedEvent;
import com.oracle.jrockit.jfr.ValueDefinition;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.tag.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("deprecation")
@EventDefinition(path = "OpenTracing/Span", name = "Open Tracing Span", description = "Open Tracing spans exposed as a JFR event", stacktrace = true, thread = true)
public class JFRSpan extends TimedEvent implements Span {

	private static final Logger LOG = LoggerFactory.getLogger(JFRSpan.class);

	private static final ExecutorService EXECUTOR = new ThreadPoolExecutor(1, 1, Long.MAX_VALUE, TimeUnit.DAYS,
			new ArrayBlockingQueue<Runnable>(50), new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			Thread thread = new Thread(r, "JFRTracer Span Events");
			thread.setDaemon(true);
			return thread;
		}
	}, new RejectedExecutionHandler() {
		@Override
		public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
			LOG.warn("Dropped JFR OpenTracing Span");
		}
	});
	private static volatile Producer producer;

	private final Span span;

	@ValueDefinition(name = "Trace ID", description = "Trace ID that will be the same for all spans that are part of the same trace", contentType = ContentType.None)
	private String traceId;

	@ValueDefinition(name = "Span ID", description = "Span ID that will be unique for every span")
	private String spanId;

	@ValueDefinition(name = "Parent Span ID", description = "ID of the parent span. Null if root span")
	private String parentSpanId;

	@ValueDefinition(name = "Operation Name", description = "Operation name of the span")
	private String operationName;

	@ValueDefinition(name = "Start Thread", description = "Thread starting the span")
	private final Thread startThread;

	@ValueDefinition(name = "Finish Thread", description = "Thread finishing the span")
	private Thread finishThread;

	private JFRSpan(Span span, String operationName, String parentSpanId) {
		this.operationName = operationName;
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

	public String getOperationName() {
		return operationName;
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
		this.operationName = operationName;
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
		this.finishThread = Thread.currentThread();
		EXECUTOR.execute(new Runnable() {
			@Override
			public void run() {
				if (JFRSpan.this.shouldWrite()) {
					JFRSpan.this.end();
					JFRSpan.this.commit();
				}
			}
		});
	}

	private static boolean init() {
		if (FlightRecorder.isActive()) {
			if (producer == null) {
				synchronized (JFRTracerImpl.class) {
					if (producer == null) {
						try {
							Producer p = new Producer("OpenTracing", "OpenTracing JFR Events", "http://opentracing.io/");
							p.addEvent(JFRSpan.class);
							p.addEvent(JFRScope.class);
							p.register();
							producer = p;
						} catch (URISyntaxException | InvalidValueException | InvalidEventDefinitionException ex) {
							LOG.error("Unable to register JFR producer.", ex);
							return false;
						}
					}
				}
			}
			return true;
		} else {
			return false;
		}
	}

	static Span createJFRSpan(Tracer tracer, Span span, String operationName, String parentSpanId) {
		if (init()) {
			final JFRSpan jfrSpan = new JFRSpan(span, operationName, parentSpanId);
			EXECUTOR.execute(new Runnable() {
				@Override
				public void run() {
					jfrSpan.begin();
				}
			});
			return jfrSpan;
		} else {
			return span;
		}
	}
}
