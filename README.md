# JFR Tracer
This is a delegating tracer to be used with OpenTracing. It records span information into the JFR, making it possible to do very deep tracing.

## Building
To build the JFR Tracer, and install it into the local maven repo, first ensure that you 
have installed an Oracle JDK 7, and an Oracle JDK 9. These will be required to build the
tracer. Once built, the tracer can be used with Oracle JDK 7+, and OpenJDK 11+.

Ensure that the following two environment variables are set to the JAVA_HOME of the JDKs:

```
JAVA_7
JAVA_9
```

Once that is done, simply run:

```bash
./gradlew publishToMavenLocal
```