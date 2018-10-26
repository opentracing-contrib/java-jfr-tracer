# JFR Tracer
This is a delegating tracer to be used with OpenTracing. It records span and scope information into the JDK Flight Recorder, enabling very deep tracing capabilities.

## Building
To build the JFR Tracer, and install it into the local maven repo, simply run:

```bash
./gradlew publishToMavenLocal
```

Note that you will need to have an Oracle JDK 7 and an Oracle JDK 9 available. Parts of the code will be compiled using JDK 7 and parts using JDK 9. Note that the resulting MRJAR will run on Oracle JDK 7 and later, and OpenJDK 11 and later.

## Using the JFR Tracer
First add a dependency to it. For example, in Maven:

```xml
	<dependency>
		<groupId>se.hirt.jmc</groupId>
		<artifactId>jfr-tracer</artifactId>
		<version>0.0.1-SNAPSHOT</version>
	</dependency>
```

Next, in whatever code you used to set up the OpenTracing tracer, wrap your tracer in the JFR tracer:

```java
GlobalTracer.register(
	new DelegatingJfrTracer(yourFavTracer));
```

## Example
An example app with OpenTracing enabled can be found here:
[https://github.com/thegreystone/problematic-microservices](https://github.com/thegreystone/problematic-microservices)

(Use the jfrtracer branch.)

## About
This tracer was built for Code One 2018 as an example of using JFR and OpenTracing together to provide cross process, and when needed very deep, tracing capabilities, using open source technologies. 

If you find this project useful, please consider contributing.
