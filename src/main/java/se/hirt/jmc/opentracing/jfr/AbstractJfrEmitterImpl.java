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

import java.util.logging.Logger;

import io.opentracing.Span;
import se.hirt.jmc.opentracing.ContextExtractor;

/**
 * Abstract super class for emitters.
 * 
 * @author Marcus Hirt
 */
abstract class AbstractJfrEmitterImpl implements JfrEmitter {
	static final Logger LOGGER = Logger.getLogger(JfrScopeEmitterImpl.class.getName());
	protected Span span;
	protected ContextExtractor extractor;
	
	AbstractJfrEmitterImpl(Span span, ContextExtractor extractor) {
		this.span = span;
		this.extractor = extractor;
	}
}
