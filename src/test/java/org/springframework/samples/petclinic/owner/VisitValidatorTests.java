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

package org.springframework.samples.petclinic.owner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

import java.time.LocalDate;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for {@link VisitValidator}
 *
 * @author AI Agent
 */
@ExtendWith(MockitoExtension.class)
@DisabledInNativeImage
public class VisitValidatorTests {

	private VisitValidator visitValidator;

	private Visit visit;

	private Errors errors;

	@BeforeEach
	void setUp() {
		visitValidator = new VisitValidator();
		visit = new Visit();
		errors = new MapBindingResult(new HashMap<>(), "visit");
	}

	@Nested
	class ValidateRejectsPastDates {

		@Test
		void shouldRejectPastDate() {
			visit.setDate(LocalDate.now().minusDays(1)); // Yesterday
			visit.setDescription("Checkup");

			visitValidator.validate(visit, errors);

			assertTrue(errors.hasFieldErrors("date"));
			assertEquals("typeMismatch.visitDate", errors.getFieldError("date").getCode());
		}

		@Test
		void shouldRejectDateOneYearAgo() {
			visit.setDate(LocalDate.now().minusYears(1)); // One year ago
			visit.setDescription("Historical data");

			visitValidator.validate(visit, errors);

			assertTrue(errors.hasFieldErrors("date"));
		}

	}

	@Nested
	class ValidateAcceptsValidDates {

		@Test
		void shouldAllowTodayDate() {
			visit.setDate(LocalDate.now()); // Today
			visit.setDescription("Checkup");

			visitValidator.validate(visit, errors);

			assertFalse(errors.hasErrors());
		}

		@Test
		void shouldAllowFutureDate() {
			visit.setDate(LocalDate.now().plusDays(7)); // Next week
			visit.setDescription("Vaccination");

			visitValidator.validate(visit, errors);

			assertFalse(errors.hasErrors());
		}

		@Test
		void shouldAllowDateOneYearAhead() {
			visit.setDate(LocalDate.now().plusYears(1)); // One year ahead
			visit.setDescription("Future appointment");

			visitValidator.validate(visit, errors);

			assertFalse(errors.hasErrors());
		}

	}

	@Nested
	class ValidateHandlesEdgeCases {

		@Test
		void shouldNotFailOnNullDate() {
			visit.setDate(null);
			visit.setDescription("Checkup");

			visitValidator.validate(visit, errors);

			// Null handling is graceful, no validation error added by validator
			assertFalse(errors.hasFieldErrors("date"));
		}

	}

}
