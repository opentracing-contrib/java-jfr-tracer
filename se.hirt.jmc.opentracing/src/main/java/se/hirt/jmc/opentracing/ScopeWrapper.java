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
package se.hirt.jmc.opentracing;

import io.opentracing.Scope;
import io.opentracing.Span;
import se.hirt.jmc.opentracing.jfr.JfrEmitter;
import se.hirt.jmc.opentracing.jfr.JfrEmitterFactory;

/**
 * @author Marcus Hirt
 */
final class ScopeWrapper implements Scope {
	private final static JfrEmitterFactory EMITTER_FACTORY = new JfrEmitterFactory();
	private final Scope delegate;
	private final JfrEmitter emitter;

	ScopeWrapper(Scope delegate) {
		this.delegate = delegate;
		emitter = EMITTER_FACTORY.create(delegate.span());
		emitter.start();
	}

	@Override
	public void close() {
		delegate.close();
		try {
			emitter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public Span span() {
		return new SpanWrapper(delegate.span());
	}
}
