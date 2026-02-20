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
 * Tests for {@link VisitStatus} enum.
 *
 * @author Claude Sonnet 4.5
 */
class VisitStatusTests {

	@Test
	@DisplayName("should allow transition from PENDING to SCHEDULED")
	void shouldAllowPendingToScheduled() {
		assertThat(VisitStatus.PENDING.canTransitionTo(VisitStatus.SCHEDULED)).isTrue();
	}

	@Test
	@DisplayName("should allow transition from PENDING to CANCELLED")
	void shouldAllowPendingToCancelled() {
		assertThat(VisitStatus.PENDING.canTransitionTo(VisitStatus.CANCELLED)).isTrue();
	}

	@Test
	@DisplayName("should not allow transition from PENDING to COMPLETED")
	void shouldNotAllowPendingToCompleted() {
		assertThat(VisitStatus.PENDING.canTransitionTo(VisitStatus.COMPLETED)).isFalse();
	}

	@Test
	@DisplayName("should allow transition from SCHEDULED to IN_PROGRESS")
	void shouldAllowScheduledToInProgress() {
		assertThat(VisitStatus.SCHEDULED.canTransitionTo(VisitStatus.IN_PROGRESS)).isTrue();
	}

	@Test
	@DisplayName("should allow transition from SCHEDULED to CANCELLED")
	void shouldAllowScheduledToCancelled() {
		assertThat(VisitStatus.SCHEDULED.canTransitionTo(VisitStatus.CANCELLED)).isTrue();
	}

	@Test
	@DisplayName("should allow transition from IN_PROGRESS to COMPLETED")
	void shouldAllowInProgressToCompleted() {
		assertThat(VisitStatus.IN_PROGRESS.canTransitionTo(VisitStatus.COMPLETED)).isTrue();
	}

	@Test
	@DisplayName("should allow transition from IN_PROGRESS to NO_SHOW")
	void shouldAllowInProgressToNoShow() {
		assertThat(VisitStatus.IN_PROGRESS.canTransitionTo(VisitStatus.NO_SHOW)).isTrue();
	}

	@Test
	@DisplayName("should not allow any transition from COMPLETED")
	void shouldNotAllowTransitionFromCompleted() {
		assertThat(VisitStatus.COMPLETED.canTransitionTo(VisitStatus.PENDING)).isFalse();
		assertThat(VisitStatus.COMPLETED.canTransitionTo(VisitStatus.SCHEDULED)).isFalse();
		assertThat(VisitStatus.COMPLETED.canTransitionTo(VisitStatus.IN_PROGRESS)).isFalse();
		assertThat(VisitStatus.COMPLETED.canTransitionTo(VisitStatus.CANCELLED)).isFalse();
	}

	@Test
	@DisplayName("should not allow any transition from CANCELLED")
	void shouldNotAllowTransitionFromCancelled() {
		assertThat(VisitStatus.CANCELLED.canTransitionTo(VisitStatus.PENDING)).isFalse();
		assertThat(VisitStatus.CANCELLED.canTransitionTo(VisitStatus.SCHEDULED)).isFalse();
	}

	@Test
	@DisplayName("should not allow any transition from NO_SHOW")
	void shouldNotAllowTransitionFromNoShow() {
		assertThat(VisitStatus.NO_SHOW.canTransitionTo(VisitStatus.PENDING)).isFalse();
		assertThat(VisitStatus.NO_SHOW.canTransitionTo(VisitStatus.COMPLETED)).isFalse();
	}

	@Test
	@DisplayName("should identify terminal states")
	void shouldIdentifyTerminalStates() {
		assertThat(VisitStatus.COMPLETED.isTerminal()).isTrue();
		assertThat(VisitStatus.CANCELLED.isTerminal()).isTrue();
		assertThat(VisitStatus.NO_SHOW.isTerminal()).isTrue();

		assertThat(VisitStatus.PENDING.isTerminal()).isFalse();
		assertThat(VisitStatus.SCHEDULED.isTerminal()).isFalse();
		assertThat(VisitStatus.IN_PROGRESS.isTerminal()).isFalse();
	}

	@Test
	@DisplayName("should have correct display names")
	void shouldHaveCorrectDisplayNames() {
		assertThat(VisitStatus.PENDING.getDisplayName()).isEqualTo("pending");
		assertThat(VisitStatus.SCHEDULED.getDisplayName()).isEqualTo("scheduled");
		assertThat(VisitStatus.IN_PROGRESS.getDisplayName()).isEqualTo("in_progress");
		assertThat(VisitStatus.COMPLETED.getDisplayName()).isEqualTo("completed");
		assertThat(VisitStatus.CANCELLED.getDisplayName()).isEqualTo("cancelled");
		assertThat(VisitStatus.NO_SHOW.getDisplayName()).isEqualTo("no_show");
	}

}
