package io.opentracing.contrib.jfrtracer.jfr;

import com.oracle.jrockit.jfr.ContentType;
import com.oracle.jrockit.jfr.EventDefinition;
import com.oracle.jrockit.jfr.TimedEvent;
import com.oracle.jrockit.jfr.ValueDefinition;
import io.opentracing.Scope;
import io.opentracing.Span;

@SuppressWarnings("deprecation")
@EventDefinition(path = "OpenTracing/Scope", name = "Open Tracing Scope", description = "Open Tracing scope exposed as a JFR event", stacktrace = true, thread = true)
public class JFRScope extends TimedEvent implements Scope {

	private final JFRScopeManager manager;
	private final Scope parent;
	private final Scope scope;
	private final JFRSpan span;
	private final boolean finishSpanOnClose;

	@ValueDefinition(name = "Trace ID", description = "Trace ID that will be the same for all spans that are part of the same trace", contentType = ContentType.None)
	private final String traceId;

	@ValueDefinition(name = "Span ID", description = "Span ID that will be unique for every span")
	private final String spanId;

	@ValueDefinition(name = "Parent Span ID", description = "ID of the parent span. Null if root span")
	private final String parentSpanId;

	@ValueDefinition(name = "Operation Name", description = "Operation name of the span")
	private final String name;

	JFRScope(JFRScopeManager manager, Scope scope, JFRSpan span, boolean finishSpanOnClose) {
		this.scope = scope;
		this.manager = manager;
		this.parent = manager.active();

		this.span = span;
		this.finishSpanOnClose = finishSpanOnClose;

		this.traceId = span.getTraceId();
		this.spanId = span.getSpanId();
		this.parentSpanId = span.getParentSpanId();
		this.name = span.getName();
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

	@Override
	public void close() {
		if (shouldWrite()) {
			end();
			commit();
		}
		scope.close();
		// The scope will close the underlying span so need to write JFR span here
		if (finishSpanOnClose) {
			span.finishJFR();
		}
		manager.setActive(parent);
	}

	@Override
	public Span span() {
		return span;
	}
}
