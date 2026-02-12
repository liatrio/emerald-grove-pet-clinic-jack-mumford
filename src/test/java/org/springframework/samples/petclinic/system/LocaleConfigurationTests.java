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

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for WebConfiguration locale setup. Tests locale resolution configuration
 * using CookieLocaleResolver and LocaleChangeInterceptor for language switching.
 *
 * @author AI Agent
 */
@SpringBootTest
class LocaleConfigurationTests {

	@Autowired
	private ApplicationContext context;

	@Test
	void shouldHaveLocaleResolverBean() {
		// Arrange & Act
		LocaleResolver resolver = context.getBean(LocaleResolver.class);

		// Assert
		assertThat(resolver).isNotNull();
		assertThat(resolver).isInstanceOf(CookieLocaleResolver.class);
	}

	@Test
	void shouldHaveLocaleChangeInterceptor() {
		// Arrange & Act
		LocaleChangeInterceptor interceptor = context.getBean(LocaleChangeInterceptor.class);

		// Assert
		assertThat(interceptor).isNotNull();
		assertThat(interceptor.getParamName()).isEqualTo("lang");
	}

}
