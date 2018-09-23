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

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.opentracing.Span;
import se.hirt.jmc.opentracing.ContextExtractor;

/**
 * Abstract super class for span emitters.
 * 
 * @author Marcus Hirt
 */
abstract class AbstractJfrSpanEmitterImpl extends AbstractJfrEmitterImpl {
	protected final static ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(1, 1, 2, TimeUnit.SECONDS,
			new LinkedBlockingQueue<Runnable>(50), new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					Thread thread = new Thread(r, "Span Events");
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
