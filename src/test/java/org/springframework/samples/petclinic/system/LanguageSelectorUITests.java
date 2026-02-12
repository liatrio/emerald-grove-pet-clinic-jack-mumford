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
 * Tests for Language Selector UI component in layout.html. Verifies that the dropdown
 * exists with all 9 languages and proper accessibility attributes.
 *
 * @author AI Agent
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class LanguageSelectorUITests {

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
	void shouldContainLanguageSelectorDropdown() {
		// Arrange & Act
		ResponseEntity<String> response = restTemplate.exchange(RequestEntity.get("/").build(), String.class);

		// Assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		String html = response.getBody();
		assertThat(html).contains("languageDropdown");
		assertThat(html).contains("fa-globe");
		assertThat(html).contains("Language selector");
	}

	@Test
	void shouldContainAllNineLanguages() {
		// Arrange & Act
		ResponseEntity<String> response = restTemplate.exchange(RequestEntity.get("/").build(), String.class);

		// Assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		String html = response.getBody();

		// Verify all 9 language options
		assertThat(html).contains("lang='en'").contains("English");
		assertThat(html).contains("lang='de'").contains("Deutsch");
		assertThat(html).contains("lang='es'").contains("Español");
		assertThat(html).contains("lang='ko'").contains("한국어");
		assertThat(html).contains("lang='fa'").contains("فارسی");
		assertThat(html).contains("lang='pt'").contains("Português");
		assertThat(html).contains("lang='ru'").contains("Русский");
		assertThat(html).contains("lang='tr'").contains("Türkçe");
		assertThat(html).contains("lang='zh'").contains("中文");
	}

	@Test
	void shouldHighlightActiveLanguage() {
		// Arrange & Act - Request page with German locale
		ResponseEntity<String> response = restTemplate.exchange(RequestEntity.get("/?lang=de").build(), String.class);

		// Assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		String html = response.getBody();

		// Verify active class is applied to German option
		assertThat(html).containsPattern("lang='de'.*active fw-bold");
	}

}
