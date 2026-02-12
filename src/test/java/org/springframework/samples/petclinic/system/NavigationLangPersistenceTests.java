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
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for URL parameter propagation. Verifies that navigation links include the lang
 * parameter to maintain language selection across page navigation.
 *
 * @author AI Agent
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class NavigationLangPersistenceTests {

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
	void shouldIncludeLangParameterInHomeLink() {
		// Arrange & Act
		ResponseEntity<String> response = restTemplate.exchange(RequestEntity.get("/?lang=de").build(), String.class);

		// Assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		String html = response.getBody();

		// Home link should include lang=de
		assertThat(html).containsPattern("href=\"/\\?lang=de\"");
	}

	@Test
	void shouldIncludeLangParameterInFindOwnersLink() {
		// Arrange & Act
		ResponseEntity<String> response = restTemplate.exchange(RequestEntity.get("/?lang=es").build(), String.class);

		// Assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		String html = response.getBody();

		// Find Owners link should include lang=es
		assertThat(html).containsPattern("href=\"/owners/find\\?lang=es\"");
	}

	@Test
	void shouldIncludeLangParameterInVetsLink() {
		// Arrange & Act
		ResponseEntity<String> response = restTemplate.exchange(RequestEntity.get("/?lang=ko").build(), String.class);

		// Assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		String html = response.getBody();

		// Vets link should include lang=ko
		assertThat(html).containsPattern("href=\"/vets\\.html\\?lang=ko\"");
	}

	@Test
	void shouldIncludeLangParameterInAllNavigationLinks() {
		// Arrange & Act
		ResponseEntity<String> response = restTemplate.exchange(RequestEntity.get("/?lang=de").build(), String.class);

		// Assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		String html = response.getBody();

		// All main navigation links should include lang=de
		assertThat(html).contains("lang=de");
		// Verify multiple occurrences (language selector + nav links)
		int langOccurrences = html.split("lang=de", -1).length - 1;
		assertThat(langOccurrences).isGreaterThan(3); // Language selector + at least 3
														// nav links
	}

}
