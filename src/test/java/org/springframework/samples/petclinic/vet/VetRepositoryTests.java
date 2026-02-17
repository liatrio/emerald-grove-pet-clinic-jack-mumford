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
package org.springframework.samples.petclinic.vet;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Integration tests for {@link VetRepository}.
 *
 * @author Claude Sonnet 4.5
 */
@DataJpaTest
class VetRepositoryTests {

	@Autowired
	private VetRepository vets;

	@Test
	void testFindBySpecialtiesName() {
		// Arrange
		Pageable pageable = PageRequest.of(0, 10);

		// Act
		Page<Vet> radiologyVets = vets.findBySpecialtiesNameIgnoreCase("radiology", pageable);

		// Assert
		assertThat(radiologyVets).isNotEmpty();
		radiologyVets.forEach(vet -> {
			assertThat(vet.getSpecialties()).isNotEmpty();
			assertThat(vet.getSpecialties().stream().anyMatch(s -> s.getName().equalsIgnoreCase("radiology"))).isTrue();
		});
	}

	@Test
	void testFindBySpecialtiesNameCaseInsensitive() {
		// Arrange
		Pageable pageable = PageRequest.of(0, 10);

		// Act
		Page<Vet> surgeryVets = vets.findBySpecialtiesNameIgnoreCase("SURGERY", pageable);

		// Assert
		assertThat(surgeryVets).isNotEmpty();
		surgeryVets.forEach(vet -> {
			assertThat(vet.getSpecialties().stream().anyMatch(s -> s.getName().equalsIgnoreCase("surgery"))).isTrue();
		});
	}

	@Test
	void testFindBySpecialtiesNameNotFound() {
		// Arrange
		Pageable pageable = PageRequest.of(0, 10);

		// Act
		Page<Vet> nonExistentVets = vets.findBySpecialtiesNameIgnoreCase("nonexistent", pageable);

		// Assert
		assertThat(nonExistentVets).isEmpty();
	}

	@Test
	void testFindAllVets() {
		// Arrange
		Pageable pageable = PageRequest.of(0, 10);

		// Act
		Page<Vet> allVets = vets.findAll(pageable);

		// Assert
		assertThat(allVets).isNotEmpty();
		assertThat(allVets.getTotalElements()).isGreaterThanOrEqualTo(6); // Sample data
																			// has 6 vets
	}

}
