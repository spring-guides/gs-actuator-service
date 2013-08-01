
## What You'll Build

This guide will take you through creating a "hello world" [RESTful web service](/understanding/REST) with Spring Boot Actuator -- we'll build a service that accepts an HTTP GET request:
```
$ curl http://localhost:9000/hello-world
```
and responds with the following [JSON](/understanding/JSON):
```
{"id":1,"content":"Hello, World!"}
```
and which also has a ton of features out of the box for managing the service in a production (or other) environment.  The business functionality of the service we build is the same as in the [REST Service Getting Started Guide](https://github.com/springframework-meta/gs-rest-service), but you don't need to have used that guide to take advantage of this one, although it might be interesting to compare the results.


## What You'll Need

 - About 15 minutes
 - A favorite text editor or IDE
 - [JDK 7][jdk] or later
 - [Maven 3.0][mvn] or later

[jdk]: http://www.oracle.com/technetwork/java/javase/downloads/index.html
[mvn]: http://maven.apache.org/download.cgi


How to complete this guide
--------------------------

Like all Spring's [Getting Started guides](/guides/gs), you can start from scratch and complete each step, or you can bypass basic setup steps that are already familiar to you. Either way, you end up with working code.

To **start from scratch**, move on to [Set up the project](#scratch).

To **skip the basics**, do the following:

 - [Download][zip] and unzip the source repository for this guide, or clone it using [git](/understanding/git):
`git clone https://github.com/springframework-meta/gs-actuator-service.git`
 - cd into `gs-actuator-service/initial`.
 - Jump ahead to [Creating a Representation Class](#initial).

**When you're finished**, you can check your results against the code in `gs-actuator-service/complete`.
[zip]: https://github.com/springframework-meta/gs-actuator-service/archive/master.zip


<a name="scratch"></a>
Set up the project
----------------------
First you set up a basic build script. You can use any build system you like when building apps with Spring, but the code you need to work with [Maven](https://maven.apache.org) and [Gradle](http://gradle.org) is included here. If you're not familiar with either, refer to [Building Java Projects with Maven](/guides/gs/maven/content) or [Building Java Projects with Gradle](/guides/gs/gradle/content).

### Create the directory structure

In a project directory of your choosing, create the following subdirectory structure; for example, with `mkdir -p src/main/java/hello` on *nix systems:

    └── src
        └── main
            └── java
                └── hello

### Create a Maven POM

`pom.xml`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>

	<groupId>org.springframework</groupId>
	<artifactId>gs-actuator-service</artifactId>
	<version>0.1.0</version>

	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>0.5.0.BUILD-SNAPSHOT</version>
	</parent>

	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-actuator</artifactId>
		</dependency>
	</dependencies>

	<repositories>
		<repository>
			<id>spring-snapshots</id>
			<name>Spring Snapshots</name>
			<url>http://repo.springsource.org/snapshot</url>
			<snapshots>
				<enabled>true</enabled>
			</snapshots>
		</repository>
	</repositories>
	<pluginRepositories>
		<pluginRepository>
			<id>spring-snapshots</id>
			<name>Spring Snapshots</name>
			<url>http://repo.springsource.org/snapshot</url>
			<snapshots>
				<enabled>true</enabled>
			</snapshots>
		</pluginRepository>
	</pluginRepositories>

</project>
```

This guide is using [Spring Boot's starter POMs](/guides/gs/spring-boot/content).

Note to experienced Maven users who are unaccustomed to using an external parent project: you can take it out later, it's just there to reduce the amount of code you have to write to get started.

Running the Service
-------------------

You can already run your service and see the Actuator features.  There is a `Spring` main class that already knows how to get the ball rolling and your Maven project is aware of it through the parent pom, so all you need to do is
```
$ mvn exec:java

...
server starts up
```
(you could also build an executable jar, as we are going to do [later in this guide](#jar)).

Wait for the server to start and go to another terminal to try it out:
```
$ curl localhost:8080
{"error":"Not Found","status":404,"message":"Not Found"}
```
So the server is running, but we haven't defined any business endpoints yet.  Instead of a default container-generated HTML error response we are seeing a generic JSON response from the Actuator `/error` endpoint.  You can see in the console logs from the server startup which endpoints are provided out of the box.  Try a few out, for example
```
$ curl localhost:8080/health
ok
```
We're "OK", so that's good.

There's more, so check out the [Actuator Project](https://github.com/SpringSource/spring-boot/tree/master/spring-bootstrap-actuator) for details.

Creating a application class
------------------------------
The first step to adding business functionality is to set up a simple Spring configuration class. It'll look like this:

`src/main/java/hello/Application.java`
```java
package hello;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableAutoConfiguration
@EnableWebMvc
@ComponentScan
public class Application {
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
} 
```

This class is concise, but there's plenty going on under the hood. [`@EnableWebMvc`](http://static.springsource.org/spring/docs/3.2.x/javadoc-api/org/springframework/web/servlet/config/annotation/EnableWebMvc.html) handles the registration of a number of components that enable Spring's support for annotation-based controllers—you'll build one of those in an upcoming step. And we've also annotated the configuration class with [`@ComponentScan`](http://static.springsource.org/spring/docs/3.2.x/javadoc-api/org/springframework/context/annotation/ComponentScan.html) which tells Spring to scan the `hello` package for those controllers (along with any other annotated component classes).


<a name="initial"></a>
Creating a Representation Class
-------------------------------
With the essential Spring MVC configuration out of the way, it's time to get to the nuts and bolts of our REST service by creating a resource representation class and an endpoint controller.

Before we get too carried away with building the endpoint controller, we need to give some thought to what our API will look like.

What we want is to handle GET requests for /hello-world, optionally with a name query parameter. In response to such a request, we'd like to send back JSON, representing a greeting, that looks something like this:

```json
{
    "id": 1,
    "content": "Hello, World!"
}
```
    
The `id` field is a unique identifier for the greeting, and `content` is the textual representation of the greeting.

To model the greeting representation, we’ll create a representation class:

`src/main/java/hello/Greeting.java`
```java
package hello;

public class Greeting {

	private final long id;
	private final String content;

	public Greeting(long id, String content) {
		this.id = id;
		this.content = content;
	}

	public long getId() {
		return id;
	}

	public String getContent() {
		return content;
	}

}
```

Now that we've got our representation class, let's create the endpoint controller that will serve it.

Creating a Resource Controller
------------------------------
In Spring, REST endpoints are just Spring MVC controllers. The following Spring MVC controller handles a GET request for /hello-world and returns our `Greeting` resource:

`src/main/java/hello/HelloWorldController.java`
```java
package hello;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/hello-world")
public class HelloWorldController {

	private static final String template = "Hello, %s!";
	private final AtomicLong counter = new AtomicLong();
	
	@RequestMapping(method=RequestMethod.GET)
	public @ResponseBody Greeting sayHello(@RequestParam(value="name", required=false, defaultValue="Stranger") String name) {
		return new Greeting(counter.incrementAndGet(), String.format(template, name));
	}
	
}
```

The key difference between a human-facing controller and a REST endpoint controller is in how the response is created. Rather than rely on a view (such as JSP) to render model data in HTML, an endpoint controller simply returns the data to be written directly to the body of the response.

The magic is in the [`@ResponseBody`](http://static.springsource.org/spring/docs/3.2.x/javadoc-api/org/springframework/web/bind/annotation/ResponseBody.html) annotation. `@ResponseBody` tells Spring MVC to not render a model into a view, but rather to write the returned object into the response body. It does this by using one of Spring's message converters. Because Jackson 2 is in the classpath, this means that [`MappingJackson2HttpMessageConverter`](http://static.springsource.org/spring/docs/3.2.x/javadoc-api/org/springframework/http/converter/json/MappingJackson2HttpMessageConverter.html) will handle the conversion of Greeting to JSON if the request's `Accept` header specifies that JSON should be returned.


Create an executable main class
---------------------------------

We can launch the application from a custom main class, or we can do that directly from one of the configuration classes.  The easiest way is to use the `SpringApplication` helper class:

`src/main/java/hello/Application.java`
```java
package hello;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableAutoConfiguration
@EnableWebMvc
@ComponentScan
public class Application {
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
} 
```

The `@EnableAutoConfiguration` annotation has also been added: it provides a load of defaults (like the embedded servlet container) depending on the contents of your classpath, and other things.

Now that your `Application` class is ready, you simply instruct the build system to create a single, executable jar containing everything. This makes it easy to ship, version, and deploy the service as an application throughout the development lifecycle, across different environments, and so forth.

Add the following configuration to your existing Maven POM:

`pom.xml`
```xml
    <properties>
        <start-class>hello.Application</start-class>
    </properties>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
```

The `start-class` property tells Maven to create a `META-INF/MANIFEST.MF` file with a `Main-Class: hello.Application` entry. This entry enables you to run it with `mvn spring-boot:run` (or simply run the jar itself with `java -jar`).

The [Spring Boot maven plugin][spring-boot-maven-plugin] collects all the jars on the classpath and builds a single "über-jar", which makes it more convenient to execute and transport your service.

Now run the following command to produce a single executable JAR file containing all necessary dependency classes and resources:

```sh
$ mvn package
```

[spring-boot-maven-plugin]: https://github.com/SpringSource/spring-boot/tree/master/spring-boot-maven-plugin

> **Note:** The procedure above will create a runnable JAR. You can also opt to [build a classic WAR file](/guides/gs/convert-jar-to-war/content) instead.

Run the service
-------------------
Run your service using the spring-boot plugin at the command line:

```sh
$ mvn spring-boot:run
```


```
... service comes up ...
```

Congratulations! You have just developed a simple RESTful service using Spring. This is a basic foundation for building a complete REST API in Spring.

Test it:

```
$ curl localhost:8080/hello-world
{"id":1,"content":"Hello, Stranger!"}
```

Switching to a different server port
-----------------------------------------

Create a properties file

`src/main/resources/application.properties`
```properties
server.port: 9000
management.port: 9001
management.address: 127.0.0.1
```

and run the server again
```
$ mvn exec:java

... service comes up on port 9001 ...
```
Test it:
```
$ curl localhost:8080/hello-world
curl: (7) couldn't connect to host
$ curl localhost:9001/hello-world
{"id":1,"content":"Hello, Stranger!"}
```

Related Resources
-----------------

There's more to Actuator and more to building RESTful web services than is covered here. You may want to continue your exploration of Spring and REST with the following Getting Started guides:

* [Getting started with REST services](https://github.com/springframework-meta/gs-rest-service)
* Handling POST, PUT, and GET requests in REST services
* Creating self-describing APIs with HATEOAS
* Securing a REST service with HTTP Basic
* Securing a REST service with OAuth
* [Consuming REST services](https://github.com/springframework-meta/gs-consuming-rest-core/blob/master/README.md)
* Testing REST services

