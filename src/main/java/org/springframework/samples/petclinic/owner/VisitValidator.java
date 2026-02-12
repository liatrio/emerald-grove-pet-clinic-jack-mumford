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

import java.time.LocalDate;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validator for Visit forms. Ensures visit dates are not in the past.
 *
 * @author AI Agent
 */
public class VisitValidator implements Validator {

	private static final String DATE_FIELD = "date";

	private static final String DATE_IN_PAST_ERROR = "typeMismatch.visitDate";

	/**
	 * Validates that the visit date is not in the past. A null date is considered valid
	 * (handled by @NotNull constraint if present on the entity).
	 * @param obj the Visit object to validate
	 * @param errors the Errors object to store validation errors
	 */
	@Override
	public void validate(Object obj, Errors errors) {
		Visit visit = (Visit) obj;
		LocalDate visitDate = visit.getDate();

		if (visitDate != null && visitDate.isBefore(LocalDate.now())) {
			errors.rejectValue(DATE_FIELD, DATE_IN_PAST_ERROR, "Visit date cannot be in the past");
		}
	}

	/**
	 * This Validator validates *just* Visit instances.
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return Visit.class.isAssignableFrom(clazz);
	}

}
