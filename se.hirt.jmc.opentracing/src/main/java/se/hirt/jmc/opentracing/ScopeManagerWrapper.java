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
import io.opentracing.ScopeManager;
import io.opentracing.Span;

/**
 * 
 * @author Marcus Hirt
 */
final class ScopeManagerWrapper implements ScopeManager {
	private final ScopeManager delegate;

	ScopeManagerWrapper(ScopeManager delegate) {
		this.delegate = delegate;
	}

	@Override
	public Scope activate(Span span, boolean finishSpanOnClose) {
		return new ScopeWrapper(delegate.activate(span, finishSpanOnClose));
	}

	@Override
	public Scope active() {
		return new ScopeWrapper(delegate.active());
	}
}
