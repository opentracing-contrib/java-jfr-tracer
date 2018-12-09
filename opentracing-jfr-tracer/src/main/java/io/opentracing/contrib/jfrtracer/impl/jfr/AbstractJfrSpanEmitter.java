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
package io.opentracing.contrib.jfrtracer.impl.jfr;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.opentracing.Span;

/**
 * Abstract super class for span emitters.
 */
abstract class AbstractJfrSpanEmitter extends AbstractJfrEmitter {

	protected final static ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(1, 1, Long.MAX_VALUE,
			TimeUnit.NANOSECONDS, new ArrayBlockingQueue<Runnable>(50), (r) -> {
				Thread thread = new Thread(r, "JfrTracer Span Events");
				thread.setDaemon(true);
				return thread;
			}, (r, e) -> {
				// Seems very unlikely to happen, but just to be sure...
				LOGGER.warning("Span Event queue full - dropped span event");
			});

	AbstractJfrSpanEmitter(Span span) {
		super(span);
	}
}
