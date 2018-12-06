package io.opentracing.contrib.jfrtracer;

import io.opentracing.Tracer;
import io.opentracing.contrib.jfrtracer.jfr.JFRTracerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JFRTracer {

	private final static Logger LOG = LoggerFactory.getLogger(JFRTracer.class);

	private JFRTracer() {
	}

	public static Tracer wrap(Tracer tracer) {
		if (isFlightRecorderAvailable()) {
			return new JFRTracerImpl(tracer);
		}
		LOG.warn("Unabled to find FlightRecorder unable wrapping tracer", new Throwable());
		return tracer;
	}

	private static boolean isFlightRecorderAvailable() {
		String[] klasses = {
			"jdk.jfr.FlightRecorder",
			"com.oracle.jrockit.jfr.FlightRecorder"
		};

		for (String klass : klasses) {
			try {
				if (Class.forName(klass, false, JFRTracer.class.getClassLoader()) != null) {
					return true;
				}
			} catch (ClassNotFoundException ex) {
			}
		}
		return false;
	}
}
