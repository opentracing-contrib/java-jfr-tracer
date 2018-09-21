package se.hirt.jmc.opentracing.jfr;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.oracle.jrockit.jfr.EventDefinition;
import com.oracle.jrockit.jfr.EventToken;
import com.oracle.jrockit.jfr.InstantEvent;
import com.oracle.jrockit.jfr.InvalidEventDefinitionException;
import com.oracle.jrockit.jfr.InvalidValueException;
import com.oracle.jrockit.jfr.Producer;
import com.oracle.jrockit.jfr.TimedEvent;
import com.oracle.jrockit.jfr.ValueDefinition;

import io.opentracing.Span;
import se.hirt.jmc.opentracing.ContextExtractor;
import se.hirt.jmc.opentracing.extractors.ExtractorRegistry;

/**
 * This is the JDK 7/8 implementation. For the JDK 9 and later implementation, see src/main/java9.
 * 
 * @author Marcus Hirt
 */
@SuppressWarnings("deprecation")
final class JfrEmitterImpl extends AbstractJfrEmitterImpl {
	private static final Producer PRODUCER;
	private static final EventToken SPAN_EVENT_TOKEN;
	private SpanEvent currentEvent;

	static {
		URI producerURI = URI.create("http://hirt.se/jfr-tracer");
		PRODUCER = new Producer("jfr-tracer", "Events produced by the OpenTracing jfr-tracer.", producerURI);
		PRODUCER.register();
		SPAN_EVENT_TOKEN = register(SpanEvent.class);
	}

	@EventDefinition(path = "jfrtracer/spanevent", name = "SpanEvent", description = "An event triggered by span activation.", stacktrace = true, thread = true)
	private static class SpanEvent extends TimedEvent {
		@ValueDefinition(name = "TraceId", description = "The trace identifier for this event.")
		private String traceId;

		@ValueDefinition(name = "SpanId", description = "The span identifier for this event.")
		private String spanId;

		@ValueDefinition(name = "ParentId", description = "The parent span identifier for this event.")
		private String parentId;

		public SpanEvent(EventToken eventToken) {
			super(eventToken);
		}

		@SuppressWarnings("unused")
		public String getTraceId() {
			return traceId;
		}

		@SuppressWarnings("unused")
		public String getSpanId() {
			return spanId;
		}

		@SuppressWarnings("unused")
		public String getParentId() {
			return parentId;
		}
	}

	/**
	 * Helper method to register an event class with the jfr-tracer producer.
	 *
	 * @param clazz
	 *            the event class to register.
	 * @return the token associated with the event class.
	 */
	static EventToken register(Class<? extends InstantEvent> clazz) {
		try {
			EventToken token = PRODUCER.addEvent(clazz);
			Logger.getLogger(JfrEmitterImpl.class.getName()).log(Level.FINE, "Registered EventType " + clazz.getName());
			return token;
		} catch (InvalidEventDefinitionException | InvalidValueException e) {
			Logger.getLogger(JfrEmitterImpl.class.getName()).log(Level.SEVERE, "Failed to register the event class "
					+ clazz.getName() + ". Event will not be available. Please check your configuration.", e);
		}
		return null;
	}

	JfrEmitterImpl(Span span) {
		super(span);
	}

	@Override
	public void close() throws Exception {
		if (currentEvent != null) {
			currentEvent.end();
			currentEvent.commit();
			currentEvent = null;
		} else {
			LOGGER.warning("Close without start discovered!");
		}
	}

	@Override
	public void start() {
		ContextExtractor extractor = ExtractorRegistry.getInstance().getCurrentExtractor();
		currentEvent = new SpanEvent(SPAN_EVENT_TOKEN);
		if (extractor != null) {
			currentEvent.traceId = extractor.extractTraceId(span);
			currentEvent.spanId = extractor.extractSpanId(span);
			currentEvent.parentId = extractor.extractParentId(span);
		} else {
			LOGGER.warning(
					"Trying to create event when no valid extractor is available. Create an extractor for your particular open tracing tracer implementation, and register it with the ExtractorRegistry.");
		}
	}
}
