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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link VisitType} enum.
 *
 * @author Claude Sonnet 4.5
 */
class VisitTypeTests {

	@Test
	@DisplayName("should have correct display names")
	void shouldHaveCorrectDisplayNames() {
		assertThat(VisitType.CHECKUP.getDisplayName()).isEqualTo("Wellness Checkup");
		assertThat(VisitType.VACCINATION.getDisplayName()).isEqualTo("Vaccination");
		assertThat(VisitType.DENTAL.getDisplayName()).isEqualTo("Dental Cleaning");
		assertThat(VisitType.SURGERY.getDisplayName()).isEqualTo("Surgery");
		assertThat(VisitType.EMERGENCY.getDisplayName()).isEqualTo("Emergency");
		assertThat(VisitType.FOLLOW_UP.getDisplayName()).isEqualTo("Follow-up");
		assertThat(VisitType.CONSULTATION.getDisplayName()).isEqualTo("Consultation");
	}

	@Test
	@DisplayName("should have correct typical durations")
	void shouldHaveCorrectTypicalDurations() {
		assertThat(VisitType.CHECKUP.getTypicalDurationMinutes()).isEqualTo(20);
		assertThat(VisitType.VACCINATION.getTypicalDurationMinutes()).isEqualTo(15);
		assertThat(VisitType.DENTAL.getTypicalDurationMinutes()).isEqualTo(60);
		assertThat(VisitType.SURGERY.getTypicalDurationMinutes()).isEqualTo(120);
		assertThat(VisitType.EMERGENCY.getTypicalDurationMinutes()).isEqualTo(0);
		assertThat(VisitType.FOLLOW_UP.getTypicalDurationMinutes()).isEqualTo(20);
		assertThat(VisitType.CONSULTATION.getTypicalDurationMinutes()).isEqualTo(30);
	}

	@Test
	@DisplayName("should have all expected visit types")
	void shouldHaveAllExpectedVisitTypes() {
		VisitType[] types = VisitType.values();
		assertThat(types).hasSize(7);
		assertThat(types).contains(VisitType.CHECKUP, VisitType.VACCINATION, VisitType.DENTAL, VisitType.SURGERY,
				VisitType.EMERGENCY, VisitType.FOLLOW_UP, VisitType.CONSULTATION);
	}

}
