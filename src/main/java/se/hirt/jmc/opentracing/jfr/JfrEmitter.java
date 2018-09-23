package se.hirt.jmc.opentracing.jfr;

/**
 * Interface for the 
 * @author Marcus Hirt
 */
public interface JfrEmitter extends AutoCloseable {
	void start();
}
