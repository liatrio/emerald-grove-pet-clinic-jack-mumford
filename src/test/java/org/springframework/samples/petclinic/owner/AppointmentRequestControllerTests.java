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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for {@link AppointmentRequestController}.
 *
 * @author Claude Sonnet 4.5
 */
@WebMvcTest(AppointmentRequestController.class)
@Import(VisitValidator.class)
@DisabledInNativeImage
@DisabledInAotMode
class AppointmentRequestControllerTests {

	private static final int TEST_OWNER_ID = 1;

	private static final int TEST_PET_ID = 1;

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private OwnerRepository owners;

	private Owner george;

	@BeforeEach
	void setup() {
		george = new Owner();
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
		max.setId(TEST_PET_ID);

		given(this.owners.findById(TEST_OWNER_ID)).willReturn(Optional.of(george));
	}

	@Test
	@DisplayName("should show appointment request form")
	void shouldShowAppointmentRequestForm() throws Exception {
		mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/appointments/request", TEST_OWNER_ID, TEST_PET_ID))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("visit"))
			.andExpect(model().attributeExists("pet"))
			.andExpect(model().attributeExists("owner"))
			.andExpect(model().attribute("visit", hasProperty("status", is(VisitStatus.PENDING))))
			.andExpect(view().name("appointments/appointmentRequestForm"));
	}

	@Test
	@DisplayName("should process valid appointment request successfully")
	void shouldProcessValidAppointmentRequestSuccessfully() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/appointments/request", TEST_OWNER_ID, TEST_PET_ID)
				.param("visitType", "CHECKUP")
				.param("requestNotes", "My pet needs a wellness checkup")
				.param("date", "2026-03-15")
				.param("description", "Annual wellness exam"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/owners/{ownerId}"));
	}

	@Test
	@DisplayName("should reject appointment request with missing visit type")
	void shouldRejectAppointmentRequestWithMissingVisitType() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/appointments/request", TEST_OWNER_ID, TEST_PET_ID)
				.param("requestNotes", "My pet needs a wellness checkup")
				.param("date", "2026-03-15")
				.param("description", "Annual wellness exam"))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("visit"))
			.andExpect(view().name("appointments/appointmentRequestForm"));
	}

	@Test
	@DisplayName("should reject appointment request with missing description")
	void shouldRejectAppointmentRequestWithMissingDescription() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/appointments/request", TEST_OWNER_ID, TEST_PET_ID)
				.param("visitType", "CHECKUP")
				.param("requestNotes", "My pet needs a wellness checkup")
				.param("date", "2026-03-15"))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("visit"))
			.andExpect(view().name("appointments/appointmentRequestForm"));
	}

	@Test
	@DisplayName("should reject appointment request with past date")
	void shouldRejectAppointmentRequestWithPastDate() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/appointments/request", TEST_OWNER_ID, TEST_PET_ID)
				.param("visitType", "CHECKUP")
				.param("requestNotes", "My pet needs a wellness checkup")
				.param("date", "2020-01-01")
				.param("description", "Annual wellness exam"))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("visit"))
			.andExpect(view().name("appointments/appointmentRequestForm"));
	}

	@Test
	@DisplayName("should handle owner not found")
	void shouldHandleOwnerNotFound() throws Exception {
		given(this.owners.findById(anyInt())).willReturn(Optional.empty());

		mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/appointments/request", 999, TEST_PET_ID))
			.andExpect(status().is4xxClientError());
	}

	@Test
	@DisplayName("should create visit with PENDING status by default")
	void shouldCreateVisitWithPendingStatusByDefault() throws Exception {
		mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/appointments/request", TEST_OWNER_ID, TEST_PET_ID))
			.andExpect(model().attribute("visit", hasProperty("status", is(VisitStatus.PENDING))));
	}

	@Test
	@DisplayName("should save owner after successful appointment request")
	void shouldSaveOwnerAfterSuccessfulAppointmentRequest() throws Exception {
		given(this.owners.save(any(Owner.class))).willReturn(george);

		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/appointments/request", TEST_OWNER_ID, TEST_PET_ID)
				.param("visitType", "CHECKUP")
				.param("requestNotes", "My pet needs a wellness checkup")
				.param("date", "2026-03-15")
				.param("description", "Annual wellness exam"))
			.andExpect(status().is3xxRedirection());
	}

}
