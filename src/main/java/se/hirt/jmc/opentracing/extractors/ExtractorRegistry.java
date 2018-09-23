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
package se.hirt.jmc.opentracing.extractors;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import io.opentracing.Span;
import io.opentracing.Tracer;
import se.hirt.jmc.opentracing.ContextExtractor;

/**
 * Registry for the plugged in {@link ContextExtractor}s.
 * 
 * @author Marcus Hirt
 */
public class ExtractorRegistry {
	private final Map<Class<? extends Span>, ContextExtractor> knownExtractorsBySpan = new HashMap<>();
	private final Map<Class<? extends Tracer>, ContextExtractor> knownExtractorsByTracer = new HashMap<>();

	private ExtractorRegistry() {
		ServiceLoader<ContextExtractor> loader = ServiceLoader.load(ContextExtractor.class,
				ExtractorRegistry.class.getClassLoader());
		for (ContextExtractor extractor : loader) {
			knownExtractorsBySpan.put(extractor.getSupportedSpanType(), extractor);
			knownExtractorsByTracer.put(extractor.getSupportedTracerType(), extractor);
		}
	}

	public static ExtractorRegistry createNewRegistry() {
		return new ExtractorRegistry();
	}

	public ContextExtractor getExtractorBySpanType(Class<? extends Span> clazz) {
		return knownExtractorsBySpan.get(clazz);
	}

	public ContextExtractor getExtractorByTracerType(Class<? extends Tracer> clazz) {
		return knownExtractorsByTracer.get(clazz);
	}
}
