# HTTP Loggger
This project is to enable the logging of the http requests and responses to and from a microservice.

Normally this is difficult because Spring Boot handles marshalling/unmarshalling of the JSON outside of our own application code.

It has been included in the maven archetype so that any new microservices created after Nov '15 will have it included.

To add this into a pre-existing microservice there are three changes required:

Include the http-logger jar in your project. This can be done either by updating to v2.1 of the parent pom or adding the http-logger dependency directly into your project pom.

Include the spring bean definition for the filter into the BootConfig.java to register the filter.

```
	/**
	 * Allows logging of request and response via logback.xml setting
	 */
	@Bean
	public Filter httpRequestResponseFilter(){
		return new HttpRequestResponseFilter();
	}
```

Add the logger to the logback.xml in your project to prepare the ability to log from the filter.

```
	<logger name="com.capgemini.psdu.httplogger" level="OFF">
		<appender-ref ref="error" />
		<appender-ref ref="application" />
	</logger>
```

Once the application is deployed with these changes, the logback.xml can be altered to change the logging level to DEBUG for the httplogger logger.