
[Spring Boot Actuator](https://github.com/SpringSource/spring-boot/tree/master/spring-boot-actuator) is a sub-project of Spring Boot. It adds several production grade services to your application with little effort on your part. In this guide, you'll build an application and then see how to add these services.

## What you'll build

This guide will take you through creating a "hello world" [RESTful web service][u-rest] with Spring Boot Actuator. You'll build a service that accepts an HTTP GET request:

```sh
$ curl http://localhost:9000/hello-world
```

It responds with the following [JSON][u-json]:

```json
{"id":1,"content":"Hello, World!"}
```

There are also has many features added to your application out-of-the-box for managing the service in a production (or other) environment.  The business functionality of the service you build is the same as in [Building a RESTful Web Service][gs-rest-service]. You don't need to use that guide to take advantage of this one, although it might be interesting to compare the results.

## What you'll need

 - About 15 minutes
 - A favorite text editor or IDE
 - [JDK 7][jdk] or later
 - [Gradle 1.7+][gradle] or [Maven 3.0+][mvn]
 - You can also import the code from this guide as well as view the web page directly into [Spring Tool Suite (STS)][gs-sts] and work your way through it from there.

[jdk]: http://www.oracle.com/technetwork/java/javase/downloads/index.html
[gradle]: http://www.gradle.org/
[mvn]: http://maven.apache.org/download.cgi
[gs-sts]: /guides/gs/sts


How to complete this guide
--------------------------

Like all Spring's [Getting Started guides](/guides/gs), you can start from scratch and complete each step, or you can bypass basic setup steps that are already familiar to you. Either way, you end up with working code.

To **start from scratch**, move on to [Set up the project](#scratch).

To **skip the basics**, do the following:

 - [Download][zip] and unzip the source repository for this guide, or clone it using [Git][u-git]:
`git clone https://github.com/spring-guides/gs-actuator-service.git`
 - cd into `gs-actuator-service/initial`.
 - Jump ahead to [Create a representation class](#initial).

**When you're finished**, you can check your results against the code in `gs-actuator-service/complete`.
[zip]: https://github.com/spring-guides/gs-actuator-service/archive/master.zip
[u-git]: /understanding/Git


<a name="scratch"></a>
Set up the project
----------------------
First you set up a basic build script. You can use any build system you like when building apps with Spring, but the code you need to work with [Gradle](http://gradle.org) and [Maven](https://maven.apache.org) is included here. If you're not familiar with either, refer to [Building Java Projects with Gradle](/guides/gs/gradle/) or [Building Java Projects with Maven](/guides/gs/maven).

### Create the directory structure

In a project directory of your choosing, create the following subdirectory structure; for example, with `mkdir -p src/main/java/hello` on *nix systems:

    └── src
        └── main
            └── java
                └── hello


### Create a Gradle build file
Below is the [initial Gradle build file](https://github.com/spring-guides/gs-actuator-service/blob/master/initial/build.gradle). But you can also use Maven. The pom.xml file is included [right here](https://github.com/spring-guides/gs-actuator-service/blob/master/initial/pom.xml). If you are using [Spring Tool Suite (STS)][gs-sts], you can import the guide directly.

`build.gradle`
```gradle
buildscript {
    repositories {
        maven { url "http://repo.springsource.org/libs-snapshot" }
        mavenLocal()
    }
}

apply plugin: 'java'
apply plugin: 'eclipse'
apply plugin: 'idea'

jar {
    baseName = 'gs-actuator-service'
    version =  '0.1.0'
}

repositories {
    mavenCentral()
    maven { url "http://repo.springsource.org/libs-snapshot" }
}

dependencies {
    compile("org.springframework.boot:spring-boot-starter-web:0.5.0.M4")
    compile("org.springframework.boot:spring-boot-starter-actuator:0.5.0.M4")
    testCompile("junit:junit:4.11")
}

task wrapper(type: Wrapper) {
    gradleVersion = '1.7'
}
```
    
[gs-sts]: /guides/gs/sts    

> **Note:** This guide is using [Spring Boot](/guides/gs/spring-boot/).

Run the service
-------------------

You can already run your service and see the Actuator features.  There is a `Spring` main class that already knows how to get the ball rolling. All you need to do is run this command.

```sh
$ ./gradlew clean build && java -jar build/libs/gs-actuator-service-0.1.0.jar
```

You haven't written any code yet, so what's happening? Wait for the server to start and go to another terminal to try it out:

```sh
$ curl localhost:8080
{"error":"Not Found","status":404,"message":"Not Found"}
```

So the server is running, but you haven't defined any business endpoints yet.  Instead of a default container-generated HTML error response you see a generic JSON response from the Actuator `/error` endpoint.  You can see in the console logs from the server startup which endpoints are provided out of the box.  Try a few out, for example
```sh
$ curl localhost:8080/health
ok
```

You're "OK", so that's good.

Check out the [Actuator Project](https://github.com/SpringSource/spring-boot/tree/master/spring-boot-actuator) for more details.

<a name="initial"></a>
Create a representation class
-------------------------------
First, give some thought to what your API will look like.

You want to handle GET requests for `/hello-world`, optionally with a name query parameter. In response to such a request, you will send back JSON, representing a greeting, that looks something like this:

```json
{
    "id": 1,
    "content": "Hello, World!"
}
```
    
The `id` field is a unique identifier for the greeting, and `content` is the textual representation of the greeting.

To model the greeting representation, create a representation class:

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

Now that you'll create the endpoint controller that will serve the representation class.

Create a resource controller
------------------------------
In Spring, REST endpoints are just Spring MVC controllers. The following Spring MVC controller handles a GET request for /hello-world and returns the `Greeting` resource:

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

The [`@ResponseBody`](http://docs.spring.io/spring/docs/3.2.x/javadoc-api/org/springframework/web/bind/annotation/ResponseBody.html) annotation tells Spring MVC not to render a model into a view, but rather to write the returned object into the response body. It does this by using one of Spring's message converters. Because Jackson 2 is in the classpath, this means that [`MappingJackson2HttpMessageConverter`](http://docs.spring.io/spring/docs/3.2.x/javadoc-api/org/springframework/http/converter/json/MappingJackson2HttpMessageConverter.html) will handle the conversion of Greeting to JSON if the request's `Accept` header specifies that JSON should be returned.


Create an executable main class
-------------------------------

You can launch the application from a custom main class, or we can do that directly from one of the configuration classes.  The easiest way is to use the `SpringApplication` helper class:

`src/main/java/hello/Application.java`
```java
package hello;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
@ComponentScan
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

} 
```

The `@EnableAutoConfiguration` annotation has also been added: it provides a load of defaults (like the embedded servlet container) depending on the contents of your classpath, and other things.

In a conventional Spring MVC application, you would add `@EnableWebMvc` to turn on key behaviors including configuration of a `DispatcherServlet`. But Spring Boot turns on this annotation automatically when it detects **spring-webmvc** on your classpath. This sets you up to build a controller in an upcoming step.

It is also annotated with [`@ComponentScan`](http://docs.spring.io/spring/docs/3.2.x/javadoc-api/org/springframework/context/annotation/ComponentScan.html), which tells Spring to scan the `hello` package for those controllers (along with any other annotated component classes).

Build an executable JAR
-----------------------
Now that your `Application` class is ready, you simply instruct the build system to create a single, executable jar containing everything. This makes it easy to ship, version, and deploy the service as an application throughout the development lifecycle, across different environments, and so forth.

Below are the Gradle steps, but if you are using Maven, you can find the updated pom.xml [right here](https://github.com/spring-guides/gs-actuator-service/blob/master/complete/pom.xml) and build it by typing `mvn clean package`.

Update your Gradle `build.gradle` file's `buildscript` section, so that it looks like this:

```groovy
buildscript {
    repositories {
        maven { url "http://repo.springsource.org/libs-snapshot" }
        mavenLocal()
    }
    dependencies {
        classpath("org.springframework.boot:spring-boot-gradle-plugin:0.5.0.M4")
    }
}
```

Further down inside `build.gradle`, add the following to the list of applied plugins:

```groovy
apply plugin: 'spring-boot'
```
You can see the final version of `build.gradle` [right here]((https://github.com/spring-guides/gs-actuator-service/blob/master/complete/build.gradle).

The [Spring Boot gradle plugin][spring-boot-gradle-plugin] collects all the jars on the classpath and builds a single "über-jar", which makes it more convenient to execute and transport your service.
It also searches for the `public static void main()` method to flag as a runnable class.

Now run the following command to produce a single executable JAR file containing all necessary dependency classes and resources:

```sh
$ ./gradlew build
```

If you are using Gradle, you can run the JAR by typing:

```sh
$ java -jar build/libs/gs-actuator-service-0.1.0.jar
```

If you are using Maven, you can run the JAR by typing:

```sh
$ java -jar target/gs-actuator-service-0.1.0.jar
```

[spring-boot-gradle-plugin]: https://github.com/SpringSource/spring-boot/tree/master/spring-boot-tools/spring-boot-gradle-plugin

> **Note:** The procedure above will create a runnable JAR. You can also opt to [build a classic WAR file](/guides/gs/convert-jar-to-war/) instead.

Run the service
-------------------
If you are using Gradle, you can run your service at the command line this way:

```sh
$ ./gradlew clean build && java -jar build/libs/gs-actuator-service-0.1.0.jar
```

> **Note:** If you are using Maven, you can run your service by typing `mvn clean package && java -jar target/gs-actuator-service-0.1.0.jar`.


```sh
... service comes up ...
```


Test it:

```sh
$ curl localhost:8080/hello-world
{"id":1,"content":"Hello, Stranger!"}
```

Switch to a different server port
-----------------------------------------

Spring Boot Acuator defaults to run on port 8080. By adding an `application.properties` file, you can override that setting.

`src/main/resources/application.properties`
```properties
server.port: 9000
management.port: 9001
management.address: 127.0.0.1
```

Run the server again:

```sh
$ ./gradlew clean build && java -jar build/libs/gs-actuator-service-0.1.0.jar

... service comes up on port 9001 ...
```
Test it:
```
$ curl localhost:8080/hello-world
curl: (7) couldn't connect to host
$ curl localhost:9001/hello-world
{"id":1,"content":"Hello, Stranger!"}
```

Summary
-----------------
Congratulations! You have just developed a simple RESTful service using Spring. You added some useful built-in services thanks to Spring Boot Actuator.

[u-rest]: /understanding/REST
[u-json]: /understanding/JSON
[gs-rest-service]: /guides/gs/rest-service
