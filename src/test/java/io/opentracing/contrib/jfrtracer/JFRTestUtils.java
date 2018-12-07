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

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.fail;

public final class JFRTestUtils {

	private JFRTestUtils() {
	}

	public static Path getJfrConfig() throws IOException {
		Path jfrConfig = Files.createTempFile("opentracing", ".jfc");
		Files.copy(JFRTestUtils.class.getResourceAsStream("opentracing.jfc"), jfrConfig, StandardCopyOption.REPLACE_EXISTING);
		return jfrConfig;
	}

	@SuppressWarnings("deprecation")
	public static void startJFR(Path jfrConfig) {
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		try {
			mbs.invoke(new ObjectName("com.sun.management:type=DiagnosticCommand"), "vmUnlockCommercialFeatures", new Object[0], new String[0]);
			mbs.invoke(new ObjectName("com.sun.management:type=DiagnosticCommand"), "jfrStart",
					new Object[] {new String[] {"name=opentracing-jfr", "settings=" + jfrConfig.toAbsolutePath().toString()}},
					new String[] {String[].class.getName()});

			assertTimeout(Duration.ofSeconds(10), () -> {
				while (JFR.get().getMBean().getRecordings().isEmpty()) {
					System.out.println("Waiting for recording to start");
					Thread.sleep(10);
				}
			});
		} catch (InstanceNotFoundException | MBeanException | MalformedObjectNameException | ReflectionException ex) {
			fail(ex.getMessage());
		}
	}

	@SuppressWarnings("deprecation")
	public static List<FLREvent> stopJfr(Path output) throws IOException {

		try {
			// Wait a little bit to make sure recording is actually capturing events since we use an executor
			Thread.sleep(100);
		} catch (InterruptedException ex) {}

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
					if (event.getPath().startsWith("OpenTracing")) {
						readAllEvents.add(event);
					} else {
						System.out.println(event.getPath());
					}
				}
			}
			return readAllEvents;
		}
	}

}
