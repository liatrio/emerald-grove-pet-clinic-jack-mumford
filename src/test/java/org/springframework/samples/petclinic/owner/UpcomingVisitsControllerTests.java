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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Unit tests for {@link UpcomingVisitsController}.
 *
 * @author Claude Sonnet 4.5
 */
@WebMvcTest(UpcomingVisitsController.class)
class UpcomingVisitsControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private VisitRepository visitRepository;

	@MockitoBean
	private PetTypeRepository petTypeRepository;

	private List<Visit> testVisits;

	@BeforeEach
	void setup() {
		// Setup pet type mock
		given(this.petTypeRepository.findAll()).willReturn(new ArrayList<>());

		// Create test owner
		Owner owner = new Owner();
		owner.setFirstName("John");
		owner.setLastName("Doe");

		// Create test pet
		Pet pet = new Pet();
		pet.setName("Max");
		pet.setOwner(owner);

		PetType type = new PetType();
		type.setName("dog");
		pet.setType(type);

		// Create test visits
		this.testVisits = new ArrayList<>();

		Visit visit1 = new Visit();
		visit1.setDate(LocalDate.now().plusDays(1));
		visit1.setDescription("Test visit 1");
		visit1.setPet(pet);

		Visit visit2 = new Visit();
		visit2.setDate(LocalDate.now().plusDays(2));
		visit2.setDescription("Test visit 2");
		visit2.setPet(pet);

		this.testVisits.add(visit1);
		this.testVisits.add(visit2);

		// Setup mock behavior
		given(this.visitRepository.findByDateGreaterThanEqualOrderByDateAsc(any(LocalDate.class)))
			.willReturn(this.testVisits);
	}

	@Test
	void testGetUpcomingVisits_returnsOkStatus() throws Exception {
		this.mockMvc.perform(get("/visits/upcoming")).andExpect(status().isOk());
	}

	@Test
	void testGetUpcomingVisits_returnsCorrectView() throws Exception {
		this.mockMvc.perform(get("/visits/upcoming")).andExpect(view().name("visits/upcomingVisits"));
	}

	@Test
	void testGetUpcomingVisits_modelContainsVisitsList() throws Exception {
		this.mockMvc.perform(get("/visits/upcoming")).andExpect(model().attributeExists("visits"));
	}

	@Test
	void testGetUpcomingVisits_emptyState() throws Exception {
		// Override mock to return empty list
		given(this.visitRepository.findByDateGreaterThanEqualOrderByDateAsc(any(LocalDate.class)))
			.willReturn(new ArrayList<>());

		this.mockMvc.perform(get("/visits/upcoming"))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("visits"));
	}

	@Test
	void testGetUpcomingVisits_withFromDateFilter() throws Exception {
		given(this.visitRepository.findUpcomingVisitsWithFilters(any(LocalDate.class), any(), any(), any()))
			.willReturn(this.testVisits);

		this.mockMvc.perform(get("/visits/upcoming").param("fromDate", "2026-02-15"))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("visits"));
	}

	@Test
	void testGetUpcomingVisits_withDateRangeFilter() throws Exception {
		given(this.visitRepository.findUpcomingVisitsWithFilters(any(LocalDate.class), any(LocalDate.class), any(),
				any()))
			.willReturn(this.testVisits);

		this.mockMvc.perform(get("/visits/upcoming").param("fromDate", "2026-02-15").param("toDate", "2026-02-20"))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("visits"));
	}

	@Test
	void testGetUpcomingVisits_withPetTypeFilter() throws Exception {
		given(this.visitRepository.findUpcomingVisitsWithFilters(any(LocalDate.class), any(), any(String.class), any()))
			.willReturn(this.testVisits);

		this.mockMvc.perform(get("/visits/upcoming").param("petType", "dog"))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("visits"));
	}

	@Test
	void testGetUpcomingVisits_withOwnerNameFilter() throws Exception {
		given(this.visitRepository.findUpcomingVisitsWithFilters(any(LocalDate.class), any(), any(), any(String.class)))
			.willReturn(this.testVisits);

		this.mockMvc.perform(get("/visits/upcoming").param("ownerLastName", "smith"))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("visits"));
	}

	@Test
	void testGetUpcomingVisits_withAllFilters() throws Exception {
		given(this.visitRepository.findUpcomingVisitsWithFilters(any(LocalDate.class), any(LocalDate.class),
				any(String.class), any(String.class)))
			.willReturn(this.testVisits);

		this.mockMvc
			.perform(get("/visits/upcoming").param("fromDate", "2026-02-15")
				.param("toDate", "2026-02-20")
				.param("petType", "dog")
				.param("ownerLastName", "smith"))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("visits"));
	}

}
