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
 * Represents the status of a veterinary visit or appointment.
 *
 * @author Claude Sonnet 4.5
 */
public enum VisitStatus {

	/**
	 * Initial state - appointment request submitted but not yet scheduled by clinic.
	 */
	PENDING("pending"),

	/**
	 * Appointment has been scheduled with specific date/time.
	 */
	SCHEDULED("scheduled"),

	/**
	 * Visit is currently in progress.
	 */
	IN_PROGRESS("in_progress"),

	/**
	 * Visit has been completed successfully.
	 */
	COMPLETED("completed"),

	/**
	 * Appointment was cancelled by owner or clinic.
	 */
	CANCELLED("cancelled"),

	/**
	 * Owner did not show up for scheduled appointment.
	 */
	NO_SHOW("no_show");

	private final String displayName;

	VisitStatus(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Checks if transition to another status is valid following the state machine:
	 * PENDING → SCHEDULED → IN_PROGRESS → COMPLETED/NO_SHOW PENDING → CANCELLED SCHEDULED
	 * → CANCELLED
	 * @param nextStatus the target status
	 * @return true if transition is allowed
	 */
	public boolean canTransitionTo(VisitStatus nextStatus) {
		return switch (this) {
			case PENDING -> nextStatus == SCHEDULED || nextStatus == CANCELLED;
			case SCHEDULED -> nextStatus == IN_PROGRESS || nextStatus == CANCELLED;
			case IN_PROGRESS -> nextStatus == COMPLETED || nextStatus == NO_SHOW;
			case COMPLETED, CANCELLED, NO_SHOW -> false; // Terminal states
		};
	}

	/**
	 * Checks if this status represents a terminal state (cannot transition further).
	 * @return true if terminal state
	 */
	public boolean isTerminal() {
		return this == COMPLETED || this == CANCELLED || this == NO_SHOW;
	}

}
