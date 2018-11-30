# JFR Tracer
This is a delegating tracer to be used with OpenTracing. It records span and scope information into the JDK Flight Recorder, enabling very deep tracing capabilities.

## Using the JFR Tracer
First add a dependency to it. For example, in Maven (note that the coordinates will change to io.opentracing.contrib once we release 0.0.4):

```xml
	<dependency>
		<groupId>se.hirt.jmc</groupId>
		<artifactId>jfr-tracer</artifactId>
		<version>0.0.3</version>
	</dependency>
```

Next, in whatever code you use to set up the OpenTracing tracer, wrap your tracer in the JFR tracer:

```java
GlobalTracer.register(
	new DelegatingJfrTracer(yourFavTracer));
```

## Supported Tracers
The JFR tracer currently supports Open Zipkin and Jaeger.

## Supported Java Versions
The JFR tracer supports running on Oracle JDK 7+ and OpenJDK 11+.

## Example
An example app with OpenTracing enabled, and which is using the JFR tracer, can be found here:
[https://github.com/thegreystone/problematic-microservices](https://github.com/thegreystone/problematic-microservices)

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

Note that you will need to have an Oracle JDK 7 and an Oracle JDK 9+ (an OpenJDK 11 will work too) available. Parts of the code will be compiled using JDK 7 and parts using JDK 9+. Note that the resulting MRJAR will run on Oracle JDK 7 and later, and OpenJDK 11 and later.



## About
This tracer was built for Code One 2018 as an example of using JFR and OpenTracing together to provide cross process, and when needed very deep, tracing capabilities, using open source technologies. 

If you find this project useful, please consider contributing.
