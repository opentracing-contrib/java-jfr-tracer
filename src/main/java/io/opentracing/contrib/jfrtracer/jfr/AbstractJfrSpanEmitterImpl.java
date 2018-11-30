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
package io.opentracing.contrib.jfrtracer.jfr;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.opentracing.Span;
import io.opentracing.contrib.jfrtracer.ContextExtractor;

/**
 * Abstract super class for span emitters.
 */
abstract class AbstractJfrSpanEmitterImpl extends AbstractJfrEmitterImpl {
	protected final static ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(1, 1, 2, TimeUnit.SECONDS,
			new LinkedBlockingQueue<Runnable>(50), new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					Thread thread = new Thread(r, "JfrTracer Span Events");
					thread.setDaemon(true);
					return thread;
				}
			}, new RejectedExecutionHandler() {
				@Override
				public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
					// Seems very unlikely to happen, but just to be sure...
					LOGGER.warning("Span Event queue full - dropped span event");
				}
			});

	AbstractJfrSpanEmitterImpl(Span span, ContextExtractor extractor) {
		super(span, extractor);
	}
}
