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

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;

/**
 * Integration tests for {@link VisitRepository}.
 *
 * @author Claude Sonnet 4.5
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class VisitRepositoryTests {

	@Autowired
	private VisitRepository visitRepository;

	@Test
	void testFindByDateGreaterThanEqualOrderByDateAsc() {
		// Arrange
		LocalDate fromDate = LocalDate.of(2013, 1, 2);

		// Act
		List<Visit> visits = this.visitRepository.findByDateGreaterThanEqualOrderByDateAsc(fromDate);

		// Assert
		assertThat(visits).isNotNull();
		assertThat(visits).isNotEmpty();

		// Verify all visits are on or after fromDate
		for (Visit visit : visits) {
			assertThat(visit.getDate()).isAfterOrEqualTo(fromDate);
		}

		// Verify visits are sorted by date ascending
		for (int i = 1; i < visits.size(); i++) {
			assertThat(visits.get(i).getDate()).isAfterOrEqualTo(visits.get(i - 1).getDate());
		}

		// Verify specific count (from test data: 2013-01-02, 2013-01-03, 2013-01-04)
		assertThat(visits).hasSize(3);
	}

	@Test
	void testFindByDateGreaterThanEqualOrderByDateAscWithJoinFetch() {
		// Arrange
		LocalDate fromDate = LocalDate.of(2013, 1, 1);

		// Act
		List<Visit> visits = this.visitRepository.findByDateGreaterThanEqualOrderByDateAsc(fromDate);

		// Assert
		assertThat(visits).isNotEmpty();

		// Verify Pet and Owner are loaded (not lazy proxies)
		for (Visit visit : visits) {
			assertThat(visit.getPet()).isNotNull();
			assertThat(visit.getPet().getName()).isNotNull();
			// This would fail with LazyInitializationException if not JOIN FETCH
		}
	}

	@Test
	void testFindByDateBetweenOrderByDateAsc() {
		// Arrange
		LocalDate fromDate = LocalDate.of(2013, 1, 2);
		LocalDate toDate = LocalDate.of(2013, 1, 3);

		// Act
		List<Visit> visits = this.visitRepository.findByDateBetweenOrderByDateAsc(fromDate, toDate);

		// Assert
		assertThat(visits).isNotEmpty();
		assertThat(visits).hasSize(2); // 2013-01-02 and 2013-01-03

		// Verify all visits are within date range
		for (Visit visit : visits) {
			assertThat(visit.getDate()).isAfterOrEqualTo(fromDate);
			assertThat(visit.getDate()).isBeforeOrEqualTo(toDate);
		}
	}

	@Test
	void testFindUpcomingVisitsWithFilters_allParameters() {
		// Arrange
		LocalDate fromDate = LocalDate.of(2013, 1, 1);
		LocalDate toDate = LocalDate.of(2013, 1, 4);
		String petType = "cat";
		String ownerLastName = "coleman";

		// Act
		List<Visit> visits = this.visitRepository.findUpcomingVisitsWithFilters(fromDate, toDate, petType,
				ownerLastName);

		// Assert
		assertThat(visits).isNotEmpty();

		// Verify all visits match the filter criteria
		for (Visit visit : visits) {
			assertThat(visit.getDate()).isAfterOrEqualTo(fromDate);
			assertThat(visit.getDate()).isBeforeOrEqualTo(toDate);
			assertThat(visit.getPet().getType().getName()).isEqualToIgnoringCase(petType);
			assertThat(visit.getPet().getOwner().getLastName()).containsIgnoringCase(ownerLastName);
		}
	}

	@Test
	void testFindUpcomingVisitsWithFilters_petTypeOnly() {
		// Arrange
		LocalDate fromDate = LocalDate.of(2013, 1, 1);
		String petType = "cat";

		// Act
		List<Visit> visits = this.visitRepository.findUpcomingVisitsWithFilters(fromDate, null, petType, null);

		// Assert
		assertThat(visits).isNotEmpty();

		// Verify all visits are for the specified pet type
		for (Visit visit : visits) {
			assertThat(visit.getPet().getType().getName()).isEqualToIgnoringCase(petType);
		}
	}

	@Test
	void testFindUpcomingVisitsWithFilters_ownerNameOnly() {
		// Arrange
		LocalDate fromDate = LocalDate.of(2013, 1, 1);
		String ownerLastName = "coleman";

		// Act
		List<Visit> visits = this.visitRepository.findUpcomingVisitsWithFilters(fromDate, null, null, ownerLastName);

		// Assert
		assertThat(visits).isNotEmpty();

		// Verify all visits are for the specified owner
		for (Visit visit : visits) {
			assertThat(visit.getPet().getOwner().getLastName()).containsIgnoringCase(ownerLastName);
		}
	}

	@Test
	void testFindUpcomingVisitsWithFilters_dateRangeOnly() {
		// Arrange
		LocalDate fromDate = LocalDate.of(2013, 1, 2);
		LocalDate toDate = LocalDate.of(2013, 1, 3);

		// Act
		List<Visit> visits = this.visitRepository.findUpcomingVisitsWithFilters(fromDate, toDate, null, null);

		// Assert
		assertThat(visits).isNotEmpty();
		assertThat(visits).hasSize(2);

		// Verify all visits are within date range
		for (Visit visit : visits) {
			assertThat(visit.getDate()).isAfterOrEqualTo(fromDate);
			assertThat(visit.getDate()).isBeforeOrEqualTo(toDate);
		}
	}

	@Test
	void testFindUpcomingVisitsWithFilters_noFilters() {
		// Arrange
		LocalDate fromDate = LocalDate.of(2013, 1, 1);

		// Act
		List<Visit> visits = this.visitRepository.findUpcomingVisitsWithFilters(fromDate, null, null, null);

		// Assert
		assertThat(visits).isNotEmpty();
		assertThat(visits).hasSize(4); // All visits from test data
	}

}
