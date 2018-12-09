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
package io.opentracing.contrib.jfrtracer;

import io.opentracing.Tracer;
import io.opentracing.contrib.jfrtracer.impl.wrapper.TracerWrapper;

import java.util.logging.Logger;

/**
 * Factory responsible for creating the wrapper tracer used to emit the flight recorder events.
 * <p>
 * Note that this is only supported API.
 */
public final class JfrTracerFactory {

	private static final Logger LOG = Logger.getLogger(JfrTracerFactory.class.getName());

	private JfrTracerFactory() {
	}

	/**
	 * Wraps a tracer in a tracer which will provide contextual JFR events The tracer will be small and the overhead
	 * small.
	 *
	 * @param delegate the tracer responsible for the normal open tracing work. This can, for example, be your usual
	 * Jaeger or Zipkin tracer.
	 * @return the wrapped tracer to use. You would normally register this tracer as your global tracer.
	 */
	public static Tracer create(Tracer delegate) {

		LOG.info("Using DelegatingJfrTracer to capture contextual information into JFR.");

		if (delegate instanceof TracerWrapper) {
			throw new IllegalArgumentException("You may not wrap a jfr tracer!");
		}

		return new TracerWrapper(delegate);
	}
}
