package se.hirt.jmc.opentracing.jfr;

import io.opentracing.Span;

/**
 * For creating JfrEmitters.
 * 
 * @author Marcus Hirt
 */
public class JfrEmitterFactory {
	public JfrEmitter create(Span span) {
		return new JfrEmitterImpl(span);
	}
}
