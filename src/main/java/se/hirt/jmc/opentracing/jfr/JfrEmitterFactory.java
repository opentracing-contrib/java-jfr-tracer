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
package se.hirt.jmc.opentracing.jfr;

import io.opentracing.Span;
import se.hirt.jmc.opentracing.ContextExtractor;

/**
 * For creating JfrEmitters.
 * 
 * @author Marcus Hirt
 */
public class JfrEmitterFactory {
	/**
	 * Thread locally emitted events for scopes. Note that the calls to {@link JfrEmitter#start()}
	 * and {@link JfrEmitter#close()} must be started and closed in the same thread.
	 * 
	 * @param span
	 *            the span containing the information to be recorded.
	 * @param extractor
	 *            the extractor to be used to extract the information from the span.
	 * @return an emitter that can be used to emit the information to JFR
	 */
	public JfrEmitter createScopeEmitter(Span span, ContextExtractor extractor) {
		return new JfrScopeEmitterImpl(span, extractor);
	}

	/**
	 * Events emitted for spans. Note that these are posted to a separate thread, and can be
	 * started/ended in different threads.
	 * 
	 * @param span
	 *            the span containing the information to be recorded.
	 * @param extractor
	 *            the extractor to be used to extract the information from the span.
	 * @return an emitter that can be used to emit the information
	 */
	public JfrEmitter createSpanEmitter(Span span, ContextExtractor extractor) {
		return new JfrSpanEmitterImpl(span, extractor);
	}
}
