package se.hirt.jmc.opentracing.extractors;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import io.opentracing.Span;
import io.opentracing.Tracer;
import se.hirt.jmc.opentracing.ContextExtractor;

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
