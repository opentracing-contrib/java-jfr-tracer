package se.hirt.jmc.opentracing.jfr;

import io.opentracing.Span;
import se.hirt.jmc.opentracing.ContextExtractor;

/**
 * For creating JfrEmitters.
 * 
 * @author Marcus Hirt
 */
public class JfrEmitterFactory {
	public JfrEmitter create(Span span, ContextExtractor extractor) {
		return new JfrEmitterImpl(span, extractor);
	}
}
