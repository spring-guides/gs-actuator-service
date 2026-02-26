/*
 * Copyright 2012-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	  https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.actuatorservice;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalManagementPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;

/**
 * Basic integration tests for service demo application.
 *
 * @author Dave Syer
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"management.server.port=0"})
@AutoConfigureRestTestClient
public class HelloWorldApplicationTests {

	@Test
	public void shouldReturn200WhenSendingRequestToController(@Autowired RestTestClient rest) {
		rest.get().uri("/hello-world").exchange().expectStatus().isOk();
	}

	@Test
	public void shouldReturn200WhenSendingRequestToManagementEndpoint(@Autowired RestTestClient rest, @LocalManagementPort int port) {
		rest.get().uri("http://localhost:{port}/actuator", port).exchange().expectStatus().isOk();
	}

}
