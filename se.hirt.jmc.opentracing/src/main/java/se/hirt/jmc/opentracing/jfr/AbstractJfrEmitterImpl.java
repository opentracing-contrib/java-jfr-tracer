package se.hirt.jmc.opentracing.jfr;

import java.util.logging.Logger;

import io.opentracing.Span;
import se.hirt.jmc.opentracing.ContextExtractor;

abstract class AbstractJfrEmitterImpl implements JfrEmitter {
	static final Logger LOGGER = Logger.getLogger(JfrEmitterImpl.class.getName());
	protected Span span;
	protected ContextExtractor extractor;
	
	AbstractJfrEmitterImpl(Span span, ContextExtractor extractor) {
		this.span = span;
		this.extractor = extractor;
	}
}
