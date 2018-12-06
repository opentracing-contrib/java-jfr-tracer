package io.opentracing.contrib.jfrtracer.jfr;

import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;
import io.opentracing.Tracer;

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
			JFRScope jfrScope = new JFRScope(this, tracer.scopeManager().activate(((JFRSpan) span).unwrap(), finishSpanOnClose), (JFRSpan) span, finishSpanOnClose);
			jfrScope.begin();
			scope = jfrScope;
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
}
