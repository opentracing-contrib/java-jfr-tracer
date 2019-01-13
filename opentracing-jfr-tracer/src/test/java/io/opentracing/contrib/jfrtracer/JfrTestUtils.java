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

import oracle.jrockit.jfr.JFR;
import oracle.jrockit.jfr.parser.ChunkParser;
import oracle.jrockit.jfr.parser.FLREvent;
import oracle.jrockit.jfr.parser.Parser;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.OpenDataException;

import static java.util.Objects.nonNull;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.fail;

@SuppressWarnings("deprecation")
public final class JfrTestUtils {

	private JfrTestUtils() {
	}

	private static Path getJfrConfig() throws IOException {
		Path jfrConfig = Files.createTempFile("opentracing", ".jfc");
		Files.copy(JfrTestUtils.class.getResourceAsStream("opentracing.jfc"), jfrConfig, StandardCopyOption.REPLACE_EXISTING);
		return jfrConfig;
	}

	public static void startJFR() {

		Path jfrConfig = null;
		try {
			jfrConfig = getJfrConfig();
		} catch (IOException ex) {
			fail(ex.getMessage());
		}

		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		final AtomicBoolean checkRecordings = new AtomicBoolean(true);
		try {
			mbs.invoke(new ObjectName("com.sun.management:type=DiagnosticCommand"), "vmUnlockCommercialFeatures", new Object[0], new String[0]);
			mbs.invoke(new ObjectName("com.sun.management:type=DiagnosticCommand"), "jfrStart",
					new Object[] {new String[] {"name=opentracing-jfr", "settings=" + jfrConfig.toAbsolutePath().toString()}},
					new String[] {String[].class.getName()});


			while (checkRecordings.get()) {
				if(JFR.get().getMBean().getRecordings().isEmpty()){
					try {
						System.out.println("Waiting for recording to start");
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} else {
					checkRecordings.set(false);
				}
			}
		} catch (OpenDataException | InstanceNotFoundException | MBeanException | MalformedObjectNameException | ReflectionException ex) {
			fail(ex.getMessage());
		} finally {
			if (nonNull(jfrConfig)) {
				try {
					Files.delete(jfrConfig);
				} catch (IOException ex) {
				}
			}
		}
	}

	public static List<FLREvent> stopJfr(Path output) throws IOException {
		try {
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			mbs.invoke(new ObjectName("com.sun.management:type=DiagnosticCommand"), "jfrStop",
					new Object[] {new String[] {"name=opentracing-jfr", "filename=" + output.toAbsolutePath().toString()}},
					new String[] {String[].class.getName()});
		} catch (InstanceNotFoundException | MBeanException | MalformedObjectNameException | ReflectionException ex) {
			fail(ex.getMessage());
		}

		try (Parser parser = new Parser(output.toFile())) {
			List<FLREvent> readAllEvents = new ArrayList<>();
			for (ChunkParser chunkParser : parser) {
				for (FLREvent event : chunkParser) {
					if (event.getPath().startsWith("opentracing")) {
						readAllEvents.add(event);
					}
				}
			}
			return readAllEvents;
		}
	}

	public static void delete(Path output) {
		try {
			Files.delete(output);
		} catch (Throwable t) {
			// Should not affect test...
			System.err.println("Failed to delete test JFR-file: " + t.getMessage());
		}
	}
}
