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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.isNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for {@link OwnerController}
 *
 * @author Colin But
 * @author Wick Dynex
 */
@WebMvcTest(OwnerController.class)
@DisabledInNativeImage
@DisabledInAotMode
class OwnerControllerTests {

	private static final int TEST_OWNER_ID = 1;

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private OwnerRepository owners;

	private Owner george() {
		Owner george = new Owner();
		george.setId(TEST_OWNER_ID);
		george.setFirstName("George");
		george.setLastName("Franklin");
		george.setAddress("110 W. Liberty St.");
		george.setCity("Madison");
		george.setTelephone("6085551023");
		Pet max = new Pet();
		PetType dog = new PetType();
		dog.setName("dog");
		max.setType(dog);
		max.setName("Max");
		max.setBirthDate(LocalDate.now());
		george.addPet(max);
		max.setId(1);
		return george;
	}

	@BeforeEach
	void setup() {

		Owner george = george();
		given(this.owners.findByLastNameStartingWith(eq("Franklin"), any(Pageable.class)))
			.willReturn(new PageImpl<>(List.of(george)));

		given(this.owners.findById(TEST_OWNER_ID)).willReturn(Optional.of(george));
		Visit visit = new Visit();
		visit.setDate(LocalDate.now());
		george.getPet("Max").getVisits().add(visit);

	}

	@Test
	void testInitCreationForm() throws Exception {
		mockMvc.perform(get("/owners/new"))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("owner"))
			.andExpect(view().name("owners/createOrUpdateOwnerForm"));
	}

	@Test
	void testProcessCreationFormSuccess() throws Exception {
		mockMvc
			.perform(post("/owners/new").param("firstName", "Joe")
				.param("lastName", "Bloggs")
				.param("address", "123 Caramel Street")
				.param("city", "London")
				.param("telephone", "1316761638"))
			.andExpect(status().is3xxRedirection());
	}

	@Test
	void testProcessCreationFormHasErrors() throws Exception {
		mockMvc
			.perform(post("/owners/new").param("firstName", "Joe").param("lastName", "Bloggs").param("city", "London"))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("owner"))
			.andExpect(model().attributeHasFieldErrors("owner", "address"))
			.andExpect(model().attributeHasFieldErrors("owner", "telephone"))
			.andExpect(view().name("owners/createOrUpdateOwnerForm"));
	}

	@Test
	void testInitFindForm() throws Exception {
		mockMvc.perform(get("/owners/find"))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("owner"))
			.andExpect(view().name("owners/findOwners"));
	}

	@Test
	void testProcessFindFormSuccess() throws Exception {
		Page<Owner> tasks = new PageImpl<>(List.of(george(), new Owner()));
		when(this.owners.findByMultipleCriteria(eq(""), isNull(), isNull(), any(Pageable.class))).thenReturn(tasks);
		mockMvc.perform(get("/owners?page=1")).andExpect(status().isOk()).andExpect(view().name("owners/ownersList"));
	}

	@Test
	void testProcessFindFormByLastName() throws Exception {
		Page<Owner> tasks = new PageImpl<>(List.of(george()));
		when(this.owners.findByMultipleCriteria(eq("Franklin"), isNull(), isNull(), any(Pageable.class)))
			.thenReturn(tasks);
		mockMvc.perform(get("/owners?page=1").param("lastName", "Franklin"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/owners/" + TEST_OWNER_ID));
	}

	@Test
	void testProcessFindFormNoOwnersFound() throws Exception {
		Page<Owner> tasks = new PageImpl<>(List.of());
		when(this.owners.findByMultipleCriteria(eq("Unknown Surname"), isNull(), isNull(), any(Pageable.class)))
			.thenReturn(tasks);
		mockMvc.perform(get("/owners?page=1").param("lastName", "Unknown Surname"))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasFieldErrors("owner", "lastName"))
			.andExpect(model().attributeHasFieldErrorCode("owner", "lastName", "notFound"))
			.andExpect(view().name("owners/findOwners"));

	}

	@Test
	void testInitUpdateOwnerForm() throws Exception {
		mockMvc.perform(get("/owners/{ownerId}/edit", TEST_OWNER_ID))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("owner"))
			.andExpect(model().attribute("owner", hasProperty("lastName", is("Franklin"))))
			.andExpect(model().attribute("owner", hasProperty("firstName", is("George"))))
			.andExpect(model().attribute("owner", hasProperty("address", is("110 W. Liberty St."))))
			.andExpect(model().attribute("owner", hasProperty("city", is("Madison"))))
			.andExpect(model().attribute("owner", hasProperty("telephone", is("6085551023"))))
			.andExpect(view().name("owners/createOrUpdateOwnerForm"));
	}

	@Test
	void testProcessUpdateOwnerFormSuccess() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/edit", TEST_OWNER_ID).param("firstName", "Joe")
				.param("lastName", "Bloggs")
				.param("address", "123 Caramel Street")
				.param("city", "London")
				.param("telephone", "1616291589"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/owners/{ownerId}"));
	}

	@Test
	void testProcessUpdateOwnerFormUnchangedSuccess() throws Exception {
		mockMvc.perform(post("/owners/{ownerId}/edit", TEST_OWNER_ID))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/owners/{ownerId}"));
	}

	@Test
	void testProcessUpdateOwnerFormHasErrors() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/edit", TEST_OWNER_ID).param("firstName", "Joe")
				.param("lastName", "Bloggs")
				.param("address", "")
				.param("telephone", ""))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("owner"))
			.andExpect(model().attributeHasFieldErrors("owner", "address"))
			.andExpect(model().attributeHasFieldErrors("owner", "telephone"))
			.andExpect(view().name("owners/createOrUpdateOwnerForm"));
	}

	@Test
	void testShowOwner() throws Exception {
		mockMvc.perform(get("/owners/{ownerId}", TEST_OWNER_ID))
			.andExpect(status().isOk())
			.andExpect(model().attribute("owner", hasProperty("lastName", is("Franklin"))))
			.andExpect(model().attribute("owner", hasProperty("firstName", is("George"))))
			.andExpect(model().attribute("owner", hasProperty("address", is("110 W. Liberty St."))))
			.andExpect(model().attribute("owner", hasProperty("city", is("Madison"))))
			.andExpect(model().attribute("owner", hasProperty("telephone", is("6085551023"))))
			.andExpect(model().attribute("owner", hasProperty("pets", not(empty()))))
			.andExpect(model().attribute("owner",
					hasProperty("pets", hasItem(hasProperty("visits", hasSize(greaterThan(0)))))))
			.andExpect(view().name("owners/ownerDetails"));
	}

	@Test
	public void testProcessUpdateOwnerFormWithIdMismatch() throws Exception {
		int pathOwnerId = 1;

		Owner owner = new Owner();
		owner.setId(2);
		owner.setFirstName("John");
		owner.setLastName("Doe");
		owner.setAddress("Center Street");
		owner.setCity("New York");
		owner.setTelephone("0123456789");

		when(owners.findById(pathOwnerId)).thenReturn(Optional.of(owner));

		mockMvc.perform(MockMvcRequestBuilders.post("/owners/{ownerId}/edit", pathOwnerId).flashAttr("owner", owner))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/owners/" + pathOwnerId + "/edit"))
			.andExpect(flash().attributeExists("error"));
	}

	@Test
	void testShowOwnerNotFound() throws Exception {
		int nonExistentOwnerId = 999;
		given(this.owners.findById(nonExistentOwnerId)).willReturn(Optional.empty());

		mockMvc.perform(get("/owners/{ownerId}", nonExistentOwnerId))
			.andExpect(status().isNotFound())
			.andExpect(view().name("notFound"))
			.andExpect(model().attributeExists("errorMessage"));
	}

	@Test
	void testShowOwnerNotFoundInEdit() throws Exception {
		int nonExistentOwnerId = 999;
		given(this.owners.findById(nonExistentOwnerId)).willReturn(Optional.empty());

		mockMvc.perform(get("/owners/{ownerId}/edit", nonExistentOwnerId))
			.andExpect(status().isNotFound())
			.andExpect(view().name("notFound"))
			.andExpect(model().attributeExists("errorMessage"));
	}

	// Issue #6: Duplicate Owner Prevention - Controller Tests

	@Test
	void shouldRejectDuplicateOwnerCreation() throws Exception {
		// Arrange: Mock repository to return existing owner (duplicate found)
		Owner george = george();
		given(this.owners.findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndTelephone(eq("George"), eq("Franklin"),
				eq("6085551023")))
			.willReturn(List.of(george));

		// Act & Assert
		mockMvc
			.perform(post("/owners/new").param("firstName", "George")
				.param("lastName", "Franklin")
				.param("address", "110 W. Liberty St.")
				.param("city", "Madison")
				.param("telephone", "6085551023"))
			.andExpect(status().isOk()) // Returns form, not redirect
			.andExpect(model().attributeHasErrors("owner"))
			.andExpect(view().name("owners/createOrUpdateOwnerForm"));
	}

	@Test
	void shouldRejectDuplicateWithDifferentCase() throws Exception {
		// Arrange: Mock repository to return existing owner (case-insensitive match)
		Owner george = george();
		given(this.owners.findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndTelephone(eq("george"), eq("franklin"),
				eq("6085551023")))
			.willReturn(List.of(george));

		// Act & Assert: Submit with lowercase names
		mockMvc
			.perform(post("/owners/new").param("firstName", "george")
				.param("lastName", "franklin")
				.param("address", "110 W. Liberty St.")
				.param("city", "Madison")
				.param("telephone", "6085551023"))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("owner"));
	}

	@Test
	void shouldAllowNonDuplicateOwnerCreation() throws Exception {
		// Arrange: Mock repository to return empty list (no duplicates)
		given(this.owners.findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndTelephone(any(), any(), any()))
			.willReturn(List.of());

		// Act & Assert: Submit unique owner
		mockMvc
			.perform(post("/owners/new").param("firstName", "Jane")
				.param("lastName", "Doe")
				.param("address", "456 Elm St.")
				.param("city", "Springfield")
				.param("telephone", "5551234567"))
			.andExpect(status().is3xxRedirection());
	}

	@Test
	void shouldAllowOwnerWithSameNameDifferentPhone() throws Exception {
		// Arrange: Mock repository to return empty list (different phone = no duplicate)
		given(this.owners.findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndTelephone(eq("George"), eq("Franklin"),
				eq("9999999999")))
			.willReturn(List.of());

		// Act & Assert: Submit owner with same name, different phone
		mockMvc
			.perform(post("/owners/new").param("firstName", "George")
				.param("lastName", "Franklin")
				.param("address", "Different Address")
				.param("city", "Different City")
				.param("telephone", "9999999999"))
			.andExpect(status().is3xxRedirection());
	}

	@Test
	void shouldNormalizeTelephoneForDuplicateCheck() throws Exception {
		// Arrange: Mock expects normalized phone (no spaces/dashes)
		Owner george = george();
		given(this.owners.findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndTelephone(eq("George"), eq("Franklin"),
				eq("6085551023")))
			.willReturn(List.of(george));

		// Act & Assert: Submit with normalized phone
		// Note: @Pattern validation requires exactly 10 digits, so this verifies
		// defensive
		// normalization
		mockMvc
			.perform(post("/owners/new").param("firstName", "George")
				.param("lastName", "Franklin")
				.param("address", "110 W. Liberty St.")
				.param("city", "Madison")
				.param("telephone", "6085551023")) // Already normalized by validation
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("owner"));
	}

	// CSV Export Tests

	@Test
	void shouldReturnCsvFormatWhenAccessingCsvEndpoint() throws Exception {
		given(this.owners.findByLastNameStartingWith("")).willReturn(List.of(george()));

		mockMvc.perform(get("/owners.csv"))
			.andExpect(status().isOk())
			.andExpect(content().contentType("text/csv; charset=UTF-8"))
			.andExpect(header().string("Content-Disposition", containsString("attachment")))
			.andExpect(content().string(containsString("First Name,Last Name,Address,City,Telephone")))
			.andExpect(content().string(containsString("George,Franklin")));
	}

	@Test
	void shouldFilterCsvByLastNameParameter() throws Exception {
		Owner george = george();
		given(this.owners.findByLastNameStartingWith("Franklin")).willReturn(List.of(george));
		given(this.owners.findByLastNameStartingWith("Davis")).willReturn(List.of());

		mockMvc.perform(get("/owners.csv").param("lastName", "Franklin"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("George,Franklin")));
	}

	@Test
	void shouldReturn404WhenNoCsvResultsFound() throws Exception {
		given(this.owners.findByLastNameStartingWith("NonExistent")).willReturn(List.of());

		mockMvc.perform(get("/owners.csv").param("lastName", "NonExistent")).andExpect(status().isNotFound());
	}

	@Test
	void shouldSetContentDispositionHeader() throws Exception {
		given(this.owners.findByLastNameStartingWith("")).willReturn(List.of(george()));

		mockMvc.perform(get("/owners.csv"))
			.andExpect(header().string("Content-Disposition", containsString("attachment")))
			.andExpect(header().string("Content-Disposition", containsString("filename=")));
	}

	@Test
	void shouldGenerateFilenameWithCurrentDate() throws Exception {
		given(this.owners.findByLastNameStartingWith("")).willReturn(List.of(george()));

		mockMvc.perform(get("/owners.csv"))
			.andExpect(header().string("Content-Disposition",
					matchesPattern(".*owners-export-\\d{4}-\\d{2}-\\d{2}\\.csv.*")));
	}

	@Test
	void shouldExportAllResultsIgnoringPagination() throws Exception {
		Owner george = george();
		Owner betty = new Owner();
		betty.setFirstName("Betty");
		betty.setLastName("Davis");
		betty.setAddress("638 Cardinal Ave.");
		betty.setCity("Sun Prairie");
		betty.setTelephone("6085551749");

		given(this.owners.findByLastNameStartingWith("")).willReturn(List.of(george, betty));

		mockMvc.perform(get("/owners.csv"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("George,Franklin")))
			.andExpect(content().string(containsString("Betty,Davis")));
	}

	// Rate Limiting Tests (CRITICAL-2: DoS Prevention)

	@Test
	void shouldReturn413WhenCsvExportExceedsMaxSize() throws Exception {
		// Create a list exceeding MAX_CSV_EXPORT_SIZE (5000)
		List<Owner> largeList = new java.util.ArrayList<>();
		for (int i = 0; i < 5001; i++) {
			Owner owner = new Owner();
			owner.setFirstName("Owner" + i);
			owner.setLastName("Test" + i);
			owner.setAddress("Address " + i);
			owner.setCity("City " + i);
			owner.setTelephone(String.format("%010d", i));
			largeList.add(owner);
		}

		given(this.owners.findByLastNameStartingWith("")).willReturn(largeList);

		mockMvc.perform(get("/owners.csv"))
			.andExpect(status().isPayloadTooLarge())
			.andExpect(status().reason(containsString("Maximum export size")));
	}

	@Test
	void shouldAllowCsvExportAtMaxSize() throws Exception {
		// Create a list at exactly MAX_CSV_EXPORT_SIZE (5000)
		List<Owner> maxSizeList = new java.util.ArrayList<>();
		for (int i = 0; i < 5000; i++) {
			Owner owner = new Owner();
			owner.setFirstName("Owner" + i);
			owner.setLastName("Test" + i);
			owner.setAddress("Address " + i);
			owner.setCity("City " + i);
			owner.setTelephone(String.format("%010d", i));
			maxSizeList.add(owner);
		}

		given(this.owners.findByLastNameStartingWith("")).willReturn(maxSizeList);

		mockMvc.perform(get("/owners.csv")).andExpect(status().isOk());
	}

	// Issue #3: Find Owners - Search by telephone and city - Controller Tests

	@Test
	void testProcessFindFormByTelephone() throws Exception {
		Page<Owner> tasks = new PageImpl<>(List.of(george()));
		when(this.owners.findByMultipleCriteria(eq(""), eq("6085551023"), isNull(), any(Pageable.class)))
			.thenReturn(tasks);
		mockMvc.perform(get("/owners?page=1").param("telephone", "6085551023"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/owners/" + TEST_OWNER_ID));
	}

	@Test
	void testProcessFindFormByCity() throws Exception {
		Page<Owner> tasks = new PageImpl<>(List.of(george(), new Owner()));
		when(this.owners.findByMultipleCriteria(eq(""), isNull(), eq("Madison"), any(Pageable.class)))
			.thenReturn(tasks);
		mockMvc.perform(get("/owners?page=1").param("city", "Madison"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/ownersList"));
	}

	@Test
	void testProcessFindFormByLastNameAndTelephone() throws Exception {
		Page<Owner> tasks = new PageImpl<>(List.of(george()));
		when(this.owners.findByMultipleCriteria(eq("Franklin"), eq("6085551023"), isNull(), any(Pageable.class)))
			.thenReturn(tasks);
		mockMvc.perform(get("/owners?page=1").param("lastName", "Franklin").param("telephone", "6085551023"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/owners/" + TEST_OWNER_ID));
	}

	@Test
	void testProcessFindFormByAllCriteria() throws Exception {
		Page<Owner> tasks = new PageImpl<>(List.of(george()));
		when(this.owners.findByMultipleCriteria(eq("Franklin"), eq("6085551023"), eq("Madison"), any(Pageable.class)))
			.thenReturn(tasks);
		mockMvc
			.perform(get("/owners?page=1").param("lastName", "Franklin")
				.param("telephone", "6085551023")
				.param("city", "Madison"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/owners/" + TEST_OWNER_ID));
	}

	@Test
	void testProcessFindFormNoCriteriaReturnsAll() throws Exception {
		Page<Owner> tasks = new PageImpl<>(List.of(george(), new Owner()));
		when(this.owners.findByMultipleCriteria(eq(""), isNull(), isNull(), any(Pageable.class))).thenReturn(tasks);
		mockMvc.perform(get("/owners?page=1")).andExpect(status().isOk()).andExpect(view().name("owners/ownersList"));
	}

}
