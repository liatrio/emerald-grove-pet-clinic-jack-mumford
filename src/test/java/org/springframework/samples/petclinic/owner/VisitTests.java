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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Visit} entity focusing on new appointment scheduling fields.
 *
 * @author Claude Sonnet 4.5
 */
class VisitTests {

	@Test
	@DisplayName("should set and get appointment time")
	void shouldSetAndGetAppointmentTime() {
		// Arrange
		Visit visit = new Visit();
		LocalDateTime appointmentTime = LocalDateTime.of(2025, 3, 15, 10, 30);

		// Act
		visit.setAppointmentTime(appointmentTime);

		// Assert
		assertThat(visit.getAppointmentTime()).isEqualTo(appointmentTime);
	}

	@Test
	@DisplayName("should have PENDING status by default")
	void shouldHavePendingStatusByDefault() {
		// Arrange & Act
		Visit visit = new Visit();

		// Assert
		assertThat(visit.getStatus()).isEqualTo(VisitStatus.PENDING);
	}

	@Test
	@DisplayName("should set and get visit status")
	void shouldSetAndGetVisitStatus() {
		// Arrange
		Visit visit = new Visit();

		// Act
		visit.setStatus(VisitStatus.SCHEDULED);

		// Assert
		assertThat(visit.getStatus()).isEqualTo(VisitStatus.SCHEDULED);
	}

	@Test
	@DisplayName("should set and get visit type")
	void shouldSetAndGetVisitType() {
		// Arrange
		Visit visit = new Visit();

		// Act
		visit.setVisitType(VisitType.CHECKUP);

		// Assert
		assertThat(visit.getVisitType()).isEqualTo(VisitType.CHECKUP);
	}

	@Test
	@DisplayName("should set and get request notes")
	void shouldSetAndGetRequestNotes() {
		// Arrange
		Visit visit = new Visit();
		String notes = "Owner prefers morning appointments";

		// Act
		visit.setRequestNotes(notes);

		// Assert
		assertThat(visit.getRequestNotes()).isEqualTo(notes);
	}

	@Test
	@DisplayName("should allow null appointment time for pending requests")
	void shouldAllowNullAppointmentTimeForPendingRequests() {
		// Arrange
		Visit visit = new Visit();
		visit.setStatus(VisitStatus.PENDING);

		// Act
		visit.setAppointmentTime(null);

		// Assert
		assertThat(visit.getAppointmentTime()).isNull();
		assertThat(visit.getStatus()).isEqualTo(VisitStatus.PENDING);
	}

	@Test
	@DisplayName("should maintain backward compatibility with existing date field")
	void shouldMaintainBackwardCompatibilityWithDateField() {
		// Arrange
		Visit visit = new Visit();
		LocalDate visitDate = LocalDate.of(2025, 3, 15);

		// Act
		visit.setDate(visitDate);

		// Assert
		assertThat(visit.getDate()).isEqualTo(visitDate);
	}

	@Test
	@DisplayName("should allow null visit type for flexibility")
	void shouldAllowNullVisitType() {
		// Arrange
		Visit visit = new Visit();

		// Act
		visit.setVisitType(null);

		// Assert
		assertThat(visit.getVisitType()).isNull();
	}

	@Test
	@DisplayName("should handle transition from PENDING to SCHEDULED")
	void shouldHandleTransitionFromPendingToScheduled() {
		// Arrange
		Visit visit = new Visit();
		assertThat(visit.getStatus()).isEqualTo(VisitStatus.PENDING);

		// Act
		visit.setStatus(VisitStatus.SCHEDULED);
		visit.setAppointmentTime(LocalDateTime.of(2025, 3, 15, 10, 30));

		// Assert
		assertThat(visit.getStatus()).isEqualTo(VisitStatus.SCHEDULED);
		assertThat(visit.getAppointmentTime()).isNotNull();
	}

	@Test
	@DisplayName("should handle complete appointment lifecycle")
	void shouldHandleCompleteAppointmentLifecycle() {
		// Arrange
		Visit visit = new Visit();
		visit.setVisitType(VisitType.CHECKUP);
		visit.setRequestNotes("Annual wellness exam");

		// Act - PENDING
		assertThat(visit.getStatus()).isEqualTo(VisitStatus.PENDING);

		// Act - SCHEDULED
		visit.setStatus(VisitStatus.SCHEDULED);
		visit.setAppointmentTime(LocalDateTime.of(2025, 3, 15, 10, 30));
		assertThat(visit.getStatus()).isEqualTo(VisitStatus.SCHEDULED);

		// Act - IN_PROGRESS
		visit.setStatus(VisitStatus.IN_PROGRESS);
		assertThat(visit.getStatus()).isEqualTo(VisitStatus.IN_PROGRESS);

		// Act - COMPLETED
		visit.setStatus(VisitStatus.COMPLETED);
		visit.setDescription("Checkup completed. Pet is healthy.");

		// Assert final state
		assertThat(visit.getStatus()).isEqualTo(VisitStatus.COMPLETED);
		assertThat(visit.getDescription()).isNotBlank();
		assertThat(visit.getVisitType()).isEqualTo(VisitType.CHECKUP);
	}

}
