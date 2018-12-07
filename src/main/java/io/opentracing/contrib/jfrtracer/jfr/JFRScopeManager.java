package io.opentracing.contrib.jfrtracer.jfr;

import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;
import io.opentracing.Tracer;

import static io.opentracing.contrib.jfrtracer.jfr.JFRScope.createJFRScope;

public class JFRScopeManager implements ScopeManager {

	final ThreadLocal<Scope> activeScope = new ThreadLocal<>();
	private final Tracer tracer;

	JFRScopeManager(Tracer tracer) {
		this.tracer = tracer;
	}

	@Override
	public Scope activate(Span span, boolean finishSpanOnClose) {
		Scope scope;
		if (span instanceof JFRSpan) {
			scope = createJFRScope(this, tracer.scopeManager().activate(((JFRSpan) span).unwrap(), finishSpanOnClose), (JFRSpan) span, finishSpanOnClose);
		} else {
			scope = new NoopScope(this, tracer.scopeManager().activate(span, finishSpanOnClose));
		}
		activeScope.set(scope);
		return scope;
	}

	@Override
	public Scope active() {
		return activeScope.get();
	}

	void setActive(Scope scope) {
		activeScope.set(scope);
	}

	@Override
	public Scope activate(Span span) {
		return activate(span, false);
	}

	@Override
	public Span activeSpan() {
		Scope scope = active();
		return scope == null ? null : scope.span();
	}
}
