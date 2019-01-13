[![Build Status](https://travis-ci.org/opentracing-contrib/java-jfr-tracer.svg?branch=master)](https://travis-ci.org/opentracing-contrib/java-jfr-tracer)
# JFR Tracer
This is a delegating tracer to be used with OpenTracing. It records span and scope information into the JDK Flight Recorder, enabling very deep tracing capabilities.

## Using the JFR Tracer
First add a dependency to it. For example, in Maven (note that this version has not been released yet, we're waiting for opentracing v0.32.0 to come on line):

```xml
	<dependency>
		<groupId>io.opentracing.contrib</groupId>
		<artifactId>opentracing-jfr-tracer</artifactId>
		<version>0.0.4</version>
	</dependency>
```

Next, in whatever code you use to set up the OpenTracing tracer, wrap your tracer in the JFR tracer:

```java
GlobalTracer.register(
	JfrTracerFactory.create(yourFavouriteTracer));
```

## Using JFR Tracer with spring
First add JFR Tracer spring autoconfig dependencies in Maven.

```xml
	<dependency>
	    <groupId>io.opentracing.contrib</groupId>
	    <artifactId>opentracing-jfr-tracer-spring-autoconfigure</artifactId>
	    <version>0.0.4</version>
    </dependency>
```
The spring-autoconfigure module will automatically wrap the available Tracer bean. In case you
want to disable set the environment variable: 
```
opentracing.jfr-tracer.enabled=false 
```

## Supported Tracers
The JFR tracer supports all tracers that support OpenTracing 0.32.0 or later.

## Supported Java Versions
The JFR tracer supports running on Oracle JDK 8+ (except Oracle JDK 9 and 10) and OpenJDK 11+.

## Example
An example app with OpenTracing enabled, and which is using the JFR tracer, can be found here:
[https://github.com/thegreystone/problematic-microservices](https://github.com/thegreystone/problematic-microservices)

## Building
To build the JFR Tracer, and install it into the local maven repo, first ensure that you 
have installed an Oracle JDK 8, and an Open JDK 11. These will be required to build the
tracer. Once built, the tracer can be used with Oracle JDK 8+ (except Oracle JDK 9 and 10), and OpenJDK 11+.

Ensure that the following two environment variables are set to the JAVA_HOME of the JDKs:

```
JAVA_8
JAVA_11
```

Once that is done, simply run:

```bash
./gradlew publishToMavenLocal
```

Note that you will need to have an Oracle JDK 8 and an OpenJDK (or Oracle JDK) JDK 11 available. Parts of the code will be compiled using JDK 8 and parts using JDK 11. Note that the resulting MRJAR will run on Oracle JDK 8 and later, and OpenJDK (or Oracle JDK) 11 and later.


## About
This tracer was built for Code One 2018 as an example of using JFR and OpenTracing together to provide cross process, and when needed very deep, tracing capabilities, using open source technologies. 

If you find this project useful, please consider contributing.
