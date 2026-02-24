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
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for {@link PendingAppointmentsController}
 *
 * @author Claude Sonnet 4.5
 */
@WebMvcTest(PendingAppointmentsController.class)
@DisabledInNativeImage
@DisabledInAotMode
class PendingAppointmentsControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private VisitRepository visits;

	private List<Visit> pendingVisits;

	@BeforeEach
	void setup() {
		// Create test data
		pendingVisits = new ArrayList<>();

		// Create first pending visit
		Owner owner1 = new Owner();
		owner1.setId(1);
		owner1.setFirstName("George");
		owner1.setLastName("Franklin");

		Pet pet1 = new Pet();
		pet1.setId(1);
		pet1.setName("Leo");
		pet1.setOwner(owner1);

		PetType petType1 = new PetType();
		petType1.setName("cat");
		pet1.setType(petType1);

		Visit visit1 = new Visit();
		visit1.setId(1);
		visit1.setDate(LocalDate.now().plusDays(3));
		visit1.setVisitType(VisitType.CHECKUP);
		visit1.setDescription("Regular wellness checkup");
		visit1.setStatus(VisitStatus.PENDING);
		visit1.setPet(pet1);

		pendingVisits.add(visit1);

		// Create second pending visit
		Owner owner2 = new Owner();
		owner2.setId(2);
		owner2.setFirstName("Betty");
		owner2.setLastName("Davis");

		Pet pet2 = new Pet();
		pet2.setId(2);
		pet2.setName("Basil");
		pet2.setOwner(owner2);

		PetType petType2 = new PetType();
		petType2.setName("hamster");
		pet2.setType(petType2);

		Visit visit2 = new Visit();
		visit2.setId(2);
		visit2.setDate(LocalDate.now().plusDays(5));
		visit2.setVisitType(VisitType.VACCINATION);
		visit2.setDescription("Annual vaccination");
		visit2.setStatus(VisitStatus.PENDING);
		visit2.setPet(pet2);

		pendingVisits.add(visit2);

		given(this.visits.findByStatusOrderByDateAsc(VisitStatus.PENDING)).willReturn(pendingVisits);
	}

	@Test
	void shouldShowPendingAppointmentsPage() throws Exception {
		mockMvc.perform(get("/appointments/pending"))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("visits"))
			.andExpect(view().name("appointments/pendingAppointments"));
	}

	@Test
	void shouldDisplayPendingVisitsInModel() throws Exception {
		mockMvc.perform(get("/appointments/pending"))
			.andExpect(status().isOk())
			.andExpect(model().attribute("visits", hasSize(2)))
			.andExpect(model().attribute("visits", hasItem(allOf(hasProperty("status", is(VisitStatus.PENDING)),
					hasProperty("visitType", is(VisitType.CHECKUP))))));
	}

	@Test
	void shouldDisplayOwnerAndPetInformation() throws Exception {
		mockMvc.perform(get("/appointments/pending"))
			.andExpect(status().isOk())
			.andExpect(model().attribute("visits", hasItem(allOf(hasProperty("pet", hasProperty("name", is("Leo"))),
					hasProperty("pet", hasProperty("owner", hasProperty("lastName", is("Franklin"))))))));
	}

	@Test
	void shouldShowEmptyListWhenNoPendingAppointments() throws Exception {
		given(this.visits.findByStatusOrderByDateAsc(VisitStatus.PENDING)).willReturn(new ArrayList<>());

		mockMvc.perform(get("/appointments/pending"))
			.andExpect(status().isOk())
			.andExpect(model().attribute("visits", hasSize(0)))
			.andExpect(view().name("appointments/pendingAppointments"));
	}

	@Test
	void shouldOrderVisitsByDateAscending() throws Exception {
		mockMvc.perform(get("/appointments/pending"))
			.andExpect(status().isOk())
			.andExpect(model().attribute("visits", hasItem(allOf(hasProperty("date", is(LocalDate.now().plusDays(3))),
					hasProperty("pet", hasProperty("name", is("Leo")))))));
	}

}
