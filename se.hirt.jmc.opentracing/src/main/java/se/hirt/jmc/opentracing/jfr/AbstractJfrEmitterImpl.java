package se.hirt.jmc.opentracing.jfr;

import io.opentracing.Span;

abstract class AbstractJfrEmitterImpl implements JfrEmitter {
	protected Span span;

	AbstractJfrEmitterImpl(Span span) {
		this.span = span;
	}
}
