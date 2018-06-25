# JFR Tracer
This is a delegating tracer to be used with OpenTracing. It records span information into the JFR, making it possible to do very deep tracing.

## Building
To build the JFR Tracer, and install it into the local maven repo, simply run:

```bash
cd 
./gradlew publishToMavenLocal
```