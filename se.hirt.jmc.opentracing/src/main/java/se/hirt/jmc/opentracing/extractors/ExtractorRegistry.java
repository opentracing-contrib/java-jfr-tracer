package se.hirt.jmc.opentracing.extractors;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import io.opentracing.Span;
import se.hirt.jmc.opentracing.ContextExtractor;

public class ExtractorRegistry {
	private static Map<Class<? extends Span>, ContextExtractor> KNOWN_EXTRACTORS = new HashMap<Class<? extends Span>, ContextExtractor>();
	private final static ExtractorRegistry INSTANCE = createNewRegistry();

	private ContextExtractor currentExtractor = KNOWN_EXTRACTORS.get(io.jaegertracing.Span.class);
	
	static {
		ServiceLoader<ContextExtractor> loader = ServiceLoader.load(ContextExtractor.class,
				ExtractorRegistry.class.getClassLoader());
		for (ContextExtractor extractor : loader) {
			KNOWN_EXTRACTORS.put(extractor.getSupportedType(), extractor);
		}
	}

	public static ExtractorRegistry getInstance() {
		return INSTANCE;
	}

	public static ExtractorRegistry createNewRegistry() {
		return new ExtractorRegistry();
	}
	
	private ExtractorRegistry() {
	}

	public ContextExtractor getExtractor(Class<? extends Span> clazz) {
		if (currentExtractor.getSupportedType() != clazz) {
			currentExtractor = KNOWN_EXTRACTORS.get(clazz);
		}
		return currentExtractor;
	}

	public ContextExtractor getCurrentExtractor() {
		return currentExtractor;
	}

}
