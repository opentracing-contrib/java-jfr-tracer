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

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.oracle.jrockit.jfr.EventToken;
import com.oracle.jrockit.jfr.InstantEvent;
import com.oracle.jrockit.jfr.InvalidEventDefinitionException;
import com.oracle.jrockit.jfr.InvalidValueException;
import com.oracle.jrockit.jfr.Producer;

@SuppressWarnings("deprecation")
public class JfrUtils {
	public static final Producer PRODUCER;

	static {
		URI producerURI = URI.create("http://hirt.se/jfr-tracer");
		PRODUCER = new Producer("jfr-tracer", "Events produced by the OpenTracing jfr-tracer.", producerURI);
		PRODUCER.register();
	}

	/**
	 * Helper method to register an event class with the jfr-tracer producer.
	 *
	 * @param clazz
	 *            the event class to register.
	 * @return the token associated with the event class.
	 */

	public static EventToken register(Class<? extends InstantEvent> clazz) {
		try {
			EventToken token = PRODUCER.addEvent(clazz);
			Logger.getLogger(JfrUtils.class.getName()).log(Level.FINE, "Registered EventType " + clazz.getName());
			return token;
		} catch (InvalidEventDefinitionException | InvalidValueException e) {
			Logger.getLogger(JfrUtils.class.getName()).log(Level.SEVERE, "Failed to register the event class "
					+ clazz.getName() + ". Event will not be available. Please check your configuration.", e);
		}
		return null;
	}
}
