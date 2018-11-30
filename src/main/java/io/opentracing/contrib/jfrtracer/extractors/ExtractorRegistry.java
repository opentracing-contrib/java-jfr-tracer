/*
 * Copyright 2018 The OpenTracing Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.opentracing.contrib.jfrtracer.extractors;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.contrib.jfrtracer.ContextExtractor;

/**
 * Registry for the plugged in {@link ContextExtractor}s.
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
