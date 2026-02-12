/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.system;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for locale switching behavior. Tests that the locale change
 * interceptor and cookie resolver work together correctly.
 *
 * @author AI Agent
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class LocaleSwitchingTests {

	@LocalServerPort
	int port;

	@Autowired
	private RestTemplateBuilder builder;

	private RestTemplate restTemplate;

	@BeforeEach
	void setUp() {
		restTemplate = builder.rootUri("http://localhost:" + port).build();
	}

	@Test
	void shouldSwitchLocaleViaQueryParameter() {
		// Arrange & Act
		ResponseEntity<String> response = restTemplate.exchange(RequestEntity.get("/?lang=de").build(), String.class);

		// Assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		// German translation should appear
		assertThat(response.getBody()).containsIgnoringCase("Tierärzte"); // "Vets" in
																			// German
	}

	@Test
	void shouldSetCookieAfterLocaleSwitch() {
		// Arrange & Act
		ResponseEntity<String> response = restTemplate.exchange(RequestEntity.get("/?lang=es").build(), String.class);

		// Assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		HttpHeaders headers = response.getHeaders();
		assertThat(headers.get(HttpHeaders.SET_COOKIE)).isNotNull();
		assertThat(headers.get(HttpHeaders.SET_COOKIE)).anyMatch(cookie -> cookie.contains("petclinic-locale=es"));
	}

	@Test
	void shouldPersistLocaleAcrossRequests() {
		// Arrange - First request: set locale to German
		ResponseEntity<String> firstResponse = restTemplate.exchange(RequestEntity.get("/?lang=de").build(),
				String.class);
		assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		// Extract cookie from first response
		String cookie = firstResponse.getHeaders()
			.get(HttpHeaders.SET_COOKIE)
			.stream()
			.filter(c -> c.contains("petclinic-locale"))
			.findFirst()
			.orElseThrow(() -> new AssertionError("Cookie not set"));

		// Act - Second request: no lang parameter, but send cookie
		ResponseEntity<String> secondResponse = restTemplate
			.exchange(RequestEntity.get("/").header(HttpHeaders.COOKIE, cookie).build(), String.class);

		// Assert - German text should still appear
		assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(secondResponse.getBody()).containsIgnoringCase("Tierärzte"); // "Vets"
																				// in
																				// German
	}

	@Test
	void shouldFallbackToEnglishForUnsupportedLanguage() {
		// Arrange & Act
		ResponseEntity<String> response = restTemplate.exchange(RequestEntity.get("/?lang=xyz").build(), String.class);

		// Assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		// English text should appear as fallback
		assertThat(response.getBody()).containsIgnoringCase("Care made modern");
	}

}
