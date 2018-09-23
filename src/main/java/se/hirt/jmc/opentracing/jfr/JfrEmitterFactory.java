/*
 * Copyright (c) 2018, Marcus Hirt
 * 
 * jfr-tracer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jfr-tracer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jfr-tracer. If not, see <http://www.gnu.org/licenses/>.
 */
package se.hirt.jmc.opentracing.jfr;

import io.opentracing.Span;
import se.hirt.jmc.opentracing.ContextExtractor;

/**
 * For creating JfrEmitters.
 * 
 * @author Marcus Hirt
 */
public class JfrEmitterFactory {
	/**
	 * Thread locally emitted events for scopes. Note that the calls to {@link JfrEmitter#start()}
	 * and {@link JfrEmitter#close()} must be started and closed in the same thread.
	 * 
	 * @param span
	 *            the span containing the information to be recorded.
	 * @param extractor
	 *            the extractor to be used to extract the information from the span.
	 * @return an emitter that can be used to emit the information to JFR
	 */
	public JfrEmitter createScopeEmitter(Span span, ContextExtractor extractor) {
		return new JfrScopeEmitterImpl(span, extractor);
	}

	/**
	 * Events emitted for spans. Note that these are posted to a separate thread, and can be
	 * started/ended in different threads.
	 * 
	 * @param span
	 *            the span containing the information to be recorded.
	 * @param extractor
	 *            the extractor to be used to extract the information from the span.
	 * @return an emitter that can be used to emit the information
	 */
	public JfrEmitter createSpanEmitter(Span span, ContextExtractor extractor) {
		return new JfrSpanEmitterImpl(span, extractor);
	}
}
