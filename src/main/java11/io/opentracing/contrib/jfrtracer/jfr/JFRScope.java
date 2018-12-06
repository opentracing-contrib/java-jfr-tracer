package io.opentracing.contrib.jfrtracer.jfr;

import io.opentracing.Scope;
import io.opentracing.Span;
import jdk.jfr.Category;
import jdk.jfr.Description;
import jdk.jfr.Label;

@Category("OpenTracing")
@Label("OpenTracing Scope")
@Description("Open Tracing spans exposed as a JFR event")
public class JFRScope extends jdk.jfr.Event implements Scope {

	private final JFRScopeManager manager;
	private final Scope parent;
	private final Scope scope;
	private final JFRSpan span;
	private final boolean finishSpanOnClose;

	@Label("Trace ID")
	@Description("Trace ID that will be the same for all spans that are part of the same trace")
	private final String traceId;

	@Label("Span ID")
	@Description("Span ID that will be unique for every span")
	private final String spanId;

	@Label("Parent Span ID")
	@Description("ID of the parent span. Null if root span")
	private final String parentSpanId;

	@Label("Operation Name")
	@Description("Operation name of the span")
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
		if (shouldCommit()) {
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
