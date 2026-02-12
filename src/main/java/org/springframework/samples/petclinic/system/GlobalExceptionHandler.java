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

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

/**
 * Global exception handler for the Pet Clinic application. Handles exceptions thrown by
 * controllers and provides user-friendly error responses.
 *
 * @author Claude Sonnet 4.5
 */
@ControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * Handles IllegalArgumentException for resource not found scenarios (missing owners
	 * or pets). Returns a user-friendly 404 page instead of exposing stack traces.
	 * @param ex the exception thrown by the controller
	 * @return ModelAndView with 404 status and user-friendly error message
	 */
	@ExceptionHandler(IllegalArgumentException.class)
	public ModelAndView handleNotFound(IllegalArgumentException ex) {
		ModelAndView mav = new ModelAndView("notFound");
		mav.setStatus(HttpStatus.NOT_FOUND);

		// Parse exception message to provide context-specific error message
		String exceptionMessage = ex.getMessage();
		String userFriendlyMessage;

		if (exceptionMessage != null && exceptionMessage.contains("Owner")) {
			userFriendlyMessage = "We couldn't find that owner. Please search again or verify the ID.";
		}
		else if (exceptionMessage != null && exceptionMessage.contains("Pet")) {
			userFriendlyMessage = "We couldn't find that pet. Please search again or verify the ID.";
		}
		else {
			// Fallback for any other IllegalArgumentException
			userFriendlyMessage = "The requested resource was not found.";
		}

		mav.addObject("errorMessage", userFriendlyMessage);
		mav.addObject("status", 404);

		return mav;
	}

}
