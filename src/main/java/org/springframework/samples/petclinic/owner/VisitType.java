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

/**
 * Represents the type of veterinary visit or appointment.
 *
 * @author Claude Sonnet 4.5
 */
public enum VisitType {

	/**
	 * Regular wellness checkup.
	 */
	CHECKUP("Wellness Checkup", 20),

	/**
	 * Vaccination appointment.
	 */
	VACCINATION("Vaccination", 15),

	/**
	 * Dental cleaning or treatment.
	 */
	DENTAL("Dental Cleaning", 60),

	/**
	 * Surgical procedure.
	 */
	SURGERY("Surgery", 120),

	/**
	 * Emergency visit (variable duration).
	 */
	EMERGENCY("Emergency", 0),

	/**
	 * Follow-up appointment.
	 */
	FOLLOW_UP("Follow-up", 20),

	/**
	 * General consultation.
	 */
	CONSULTATION("Consultation", 30);

	private final String displayName;

	private final int typicalDurationMinutes;

	VisitType(String displayName, int typicalDurationMinutes) {
		this.displayName = displayName;
		this.typicalDurationMinutes = typicalDurationMinutes;
	}

	public String getDisplayName() {
		return displayName;
	}

	public int getTypicalDurationMinutes() {
		return typicalDurationMinutes;
	}

}
