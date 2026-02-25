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

import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalManagementPort;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.BDDAssertions.then;

/**
 * Basic integration tests for service demo application.
 *
 * @author Dave Syer
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"management.server.port=0"})
public class HelloWorldApplicationTests {

	@LocalServerPort
	private int port;

	@LocalManagementPort
	private int mgt;

	@Autowired
	private RestClient.Builder restClientBuilder;

	@Test
	public void shouldReturn200WhenSendingRequestToController() throws Exception {
		RestClient restClient = this.restClientBuilder
				.baseUrl("http://localhost:" + this.port)
				.build();
		@SuppressWarnings("rawtypes")
		ResponseEntity<Map> entity = restClient.get()
				.uri("/hello-world")
				.retrieve()
				.toEntity(Map.class);

		then(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	public void shouldReturn200WhenSendingRequestToManagementEndpoint() throws Exception {
		RestClient restClient = this.restClientBuilder
				.baseUrl("http://localhost:" + this.mgt)
				.build();
		@SuppressWarnings("rawtypes")
		ResponseEntity<Map> entity = restClient.get()
				.uri("/actuator")
				.retrieve()
				.toEntity(Map.class);
		then(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

}
