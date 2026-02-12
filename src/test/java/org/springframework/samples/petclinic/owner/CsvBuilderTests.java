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

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link CsvBuilder}.
 *
 * @author Jack Mumford
 */
class CsvBuilderTests {

	@Test
	void shouldGenerateHeaderRow() {
		String csv = CsvBuilder.buildOwnersCsv(Collections.emptyList());
		assertThat(csv).startsWith("First Name,Last Name,Address,City,Telephone\n");
	}

	@Test
	void shouldFormatSingleOwnerRow() {
		Owner owner = createTestOwner("George", "Franklin", "110 W. Liberty St.", "Madison", "6085551023");
		String csv = CsvBuilder.buildOwnersCsv(List.of(owner));
		assertThat(csv).contains("George,Franklin,110 W. Liberty St.,Madison,6085551023");
	}

	@Test
	void shouldEscapeFieldsWithCommas() {
		Owner owner = createTestOwner("Betty", "Davis", "638 Cardinal Ave., Apt 2B", "Sun Prairie", "6085551749");
		String csv = CsvBuilder.buildOwnersCsv(List.of(owner));
		assertThat(csv).contains("\"638 Cardinal Ave., Apt 2B\"");
	}

	@Test
	void shouldEscapeFieldsWithQuotes() {
		Owner owner = createTestOwner("John", "O'Brien", "123 Main St.", "Madison", "6085551234");
		String csv = CsvBuilder.buildOwnersCsv(List.of(owner));
		// Single quotes don't need escaping, but double quotes would
		assertThat(csv).contains("John,O'Brien");

		Owner owner2 = createTestOwner("Jane", "Test\"Name", "456 Oak Ave.", "Madison", "6085555678");
		String csv2 = CsvBuilder.buildOwnersCsv(List.of(owner2));
		assertThat(csv2).contains("\"Test\"\"Name\"");
	}

	@Test
	void shouldEscapeFieldsWithNewlines() {
		Owner owner = createTestOwner("Carlos", "Estaban", "789 Pine St.\nApt 5", "Madison", "6085559999");
		String csv = CsvBuilder.buildOwnersCsv(List.of(owner));
		assertThat(csv).contains("\"789 Pine St.\nApt 5\"");
	}

	@Test
	void shouldHandleNullFields() {
		Owner owner = new Owner();
		owner.setFirstName("John");
		owner.setLastName("Doe");
		// address, city, telephone will be null
		String csv = CsvBuilder.buildOwnersCsv(List.of(owner));
		assertThat(csv).contains("John,Doe,,,");
	}

	@Test
	void shouldHandleUnicodeCharacters() {
		Owner owner = createTestOwner("José", "Müller", "123 Main St.", "Zürich", "6085551234");
		String csv = CsvBuilder.buildOwnersCsv(List.of(owner));
		assertThat(csv).contains("José,Müller,123 Main St.,Zürich,6085551234");
	}

	@Test
	void shouldFormatMultipleOwnerRows() {
		Owner owner1 = createTestOwner("George", "Franklin", "110 W. Liberty St.", "Madison", "6085551023");
		Owner owner2 = createTestOwner("Betty", "Davis", "638 Cardinal Ave., Apt 2B", "Sun Prairie", "6085551749");
		Owner owner3 = createTestOwner("Eduardo", "Rodriquez", "2693 Commerce St.", "McFarland", "6085558763");

		String csv = CsvBuilder.buildOwnersCsv(List.of(owner1, owner2, owner3));

		assertThat(csv).startsWith("First Name,Last Name,Address,City,Telephone\n");
		assertThat(csv).contains("George,Franklin,110 W. Liberty St.,Madison,6085551023");
		assertThat(csv).contains("\"638 Cardinal Ave., Apt 2B\"");
		assertThat(csv).contains("Eduardo,Rodriquez,2693 Commerce St.,McFarland,6085558763");
	}

	private Owner createTestOwner(String firstName, String lastName, String address, String city, String telephone) {
		Owner owner = new Owner();
		owner.setFirstName(firstName);
		owner.setLastName(lastName);
		owner.setAddress(address);
		owner.setCity(city);
		owner.setTelephone(telephone);
		return owner;
	}

}
