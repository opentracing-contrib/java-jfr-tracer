package se.hirt.jmc.opentracing.jfr;

import java.util.logging.Logger;

import io.opentracing.Span;

abstract class AbstractJfrEmitterImpl implements JfrEmitter {
	static final Logger LOGGER = Logger.getLogger(JfrEmitterImpl.class.getName());
	protected Span span;

	AbstractJfrEmitterImpl(Span span) {
		this.span = span;
	}
}
