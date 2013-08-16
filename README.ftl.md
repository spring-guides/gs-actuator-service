<#assign project_id="gs-actuator-service">

## What you'll build

<@u_rest/>
```
$ curl http://localhost:9000/hello-world
```
<@u_json/>
```
{"id":1,"content":"Hello, World!"}
```
and which also has many features out-of-the-box for managing the service in a production (or other) environment.  The business functionality of the service you build is the same as in [Building a RESTful Web Service][gs-rest-service]. You don't need to use that guide to take advantage of this one, although it might be interesting to compare the results.


## What you'll need

 - About 15 minutes
 - <@prereq_editor_jdk_buildtools java_version="7"/>


## <@how_to_complete_this_guide jump_ahead='Create a representation class'/>


<a name="scratch"></a>
Set up the project
----------------------
<@build_system_intro/>

<@create_directory_structure_hello/>

### Create a Maven POM

    <@snippet path="pom.xml" prefix="initial"/>

<@bootstrap_starter_pom_disclaimer/>

Run the service
-------------------

You can already run your service and see the Actuator features.  There is a `Spring` main class that already knows how to get the ball rolling and your Maven project is aware of it through the parent pom, so all you need to do is run this command.
```
$ mvn exec:java

...
server starts up
```
You could also build an executable jar, as we are going to do later in this guide.

Wait for the server to start and go to another terminal to try it out:
```
$ curl localhost:8080
{"error":"Not Found","status":404,"message":"Not Found"}
```
So the server is running, but you haven't defined any business endpoints yet.  Instead of a default container-generated HTML error response you see a generic JSON response from the Actuator `/error` endpoint.  You can see in the console logs from the server startup which endpoints are provided out of the box.  Try a few out, for example
```
$ curl localhost:8080/health
ok
```
We're "OK", so that's good.

Check out the [Actuator Project](https://github.com/SpringSource/spring-boot/tree/master/spring-boot-actuator) for more details.

Create an application class
------------------------------
The first step to adding business functionality is to set up a simple Spring configuration class. It'll look like this:

    <@snippet path="src/main/java/hello/Application.java" prefix="complete"/>

This class is concise, but there's plenty going on under the hood. [`@EnableWebMvc`](http://static.springsource.org/spring/docs/3.2.x/javadoc-api/org/springframework/web/servlet/config/annotation/EnableWebMvc.html) handles the registration of a number of components that enable Spring's support for annotation-based controllers. You'll build a controller in an upcoming step. 

The configuration class is also annotated with [`@ComponentScan`](http://static.springsource.org/spring/docs/3.2.x/javadoc-api/org/springframework/context/annotation/ComponentScan.html), which tells Spring to scan the `hello` package for those controllers (along with any other annotated component classes).


<a name="initial"></a>
Create a representation class
-------------------------------
With the essential Spring MVC configuration out of the way, it's time to get to the nuts and bolts of our REST service by creating a resource representation class and an endpoint controller.

First, give some thought to what your API will look like.

You want to handle GET requests for /hello-world, optionally with a name query parameter. In response to such a request, you will send back JSON, representing a greeting, that looks something like this:

```json
{
    "id": 1,
    "content": "Hello, World!"
}
```
    
The `id` field is a unique identifier for the greeting, and `content` is the textual representation of the greeting.

To model the greeting representation, create a representation class:

    <@snippet path="src/main/java/hello/Greeting.java" prefix="complete"/>

Now that you'll create the endpoint controller that will serve the representation class.

Create a resource controller
------------------------------
In Spring, REST endpoints are just Spring MVC controllers. The following Spring MVC controller handles a GET request for /hello-world and returns the `Greeting` resource:

    <@snippet path="src/main/java/hello/HelloWorldController.java" prefix="complete"/>

The key difference between a human-facing controller and a REST endpoint controller is in how the response is created. Rather than rely on a view (such as JSP) to render model data in HTML, an endpoint controller simply returns the data to be written directly to the body of the response.

The [`@ResponseBody`](http://static.springsource.org/spring/docs/3.2.x/javadoc-api/org/springframework/web/bind/annotation/ResponseBody.html) annotation tells Spring MVC not to render a model into a view, but rather to write the returned object into the response body. It does this by using one of Spring's message converters. Because Jackson 2 is in the classpath, this means that [`MappingJackson2HttpMessageConverter`](http://static.springsource.org/spring/docs/3.2.x/javadoc-api/org/springframework/http/converter/json/MappingJackson2HttpMessageConverter.html) will handle the conversion of Greeting to JSON if the request's `Accept` header specifies that JSON should be returned.


Create an executable main class
---------------------------------

You can launch the application from a custom main class, or we can do that directly from one of the configuration classes.  The easiest way is to use the `SpringApplication` helper class:

    <@snippet path="src/main/java/hello/Application.java" prefix="complete"/>

The `@EnableAutoConfiguration` annotation has also been added: it provides a load of defaults (like the embedded servlet container) depending on the contents of your classpath, and other things.

<@build_an_executable_jar_mainhead/>
<@build_an_executable_jar/>

<@run_the_application_with_maven module="service"/>

```
... service comes up ...
```


Test it:

```
$ curl localhost:8080/hello-world
{"id":1,"content":"Hello, Stranger!"}
```

Switch to a different server port
-----------------------------------------

Create a properties file:

    <@snippet path="src/main/resources/application.properties" prefix="complete"/>

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

Summary
-----------------
Congratulations! You have just developed a simple RESTful service using Spring. This is a basic foundation for building a complete REST API in Spring.