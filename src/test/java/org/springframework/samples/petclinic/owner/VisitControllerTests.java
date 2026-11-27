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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

/**
 * Test class for {@link VisitController}
 *
 * @author Colin But
 * @author Wick Dynex
 */
@WebMvcTest(VisitController.class)
@DisabledInNativeImage
@DisabledInAotMode
class VisitControllerTests {

	private static final int TEST_OWNER_ID = 1;

	private static final int TEST_PET_ID = 1;

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private OwnerRepository owners;

	private Pet pet;

	@BeforeEach
	void init() {
		Owner owner = new Owner();
		pet = new Pet();
		owner.addPet(pet);
		pet.setId(TEST_PET_ID);
		given(this.owners.findById(TEST_OWNER_ID)).willReturn(Optional.of(owner));
	}

	@Test
	void testInitNewVisitForm() throws Exception {
		mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID))
			.andExpect(status().isOk())
			.andExpect(view().name("pets/createOrUpdateVisitForm"));
	}

	@Test
	void testProcessNewVisitFormSuccess() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
				.param("name", "George")
				.param("description", "Visit Description"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/owners/{ownerId}"));
	}

	@Test
	void testProcessNewVisitFormHasErrors() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID).param("name",
					"George"))
			.andExpect(model().attributeHasErrors("visit"))
			.andExpect(status().isOk())
			.andExpect(view().name("pets/createOrUpdateVisitForm"));
	}

	@Test
	void testInitNewVisitFormExposesOwnerAndPet() throws Exception {
		mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID))
			.andExpect(status().isOk())
			.andExpect(model().attribute("pet", pet))
			.andExpect(model().attributeExists("owner", "visit"));
	}

	@Test
	void testInitNewVisitFormOwnerNotFound() {
		assertThatThrownBy(() -> mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/visits/new", 99, TEST_PET_ID)))
			.hasCauseInstanceOf(IllegalArgumentException.class)
			.hasRootCauseMessage("Owner not found with id: 99. Please ensure the ID is correct ");
	}

	@Test
	void testInitNewVisitFormPetNotFound() {
		assertThatThrownBy(() -> mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, 99)))
			.hasCauseInstanceOf(IllegalArgumentException.class)
			.hasRootCauseMessage("Pet with id 99 not found for owner with id 1.");
	}

	@Test
	void testProcessNewVisitFormOwnerNotFound() {
		assertThatThrownBy(() -> mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", 99, TEST_PET_ID)
			.param("description", "Visit Description"))).hasCauseInstanceOf(IllegalArgumentException.class);
		verify(this.owners, never()).save(any(Owner.class));
	}

	@Test
	void testProcessNewVisitFormSavesVisitAndAddsFlashMessage() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
				.param("date", "2024-03-15")
				.param("description", "Visit Description"))
			.andExpect(status().is3xxRedirection())
			.andExpect(flash().attribute("message", "Your visit has been booked"))
			.andExpect(view().name("redirect:/owners/{ownerId}"));

		ArgumentCaptor<Owner> savedOwner = ArgumentCaptor.forClass(Owner.class);
		verify(this.owners).save(savedOwner.capture());
		Pet savedPet = savedOwner.getValue().getPet(TEST_PET_ID);
		assertThat(savedPet.getVisits()).singleElement().satisfies(visit -> {
			assertThat(visit.getDescription()).isEqualTo("Visit Description");
			assertThat(visit.getDate()).hasToString("2024-03-15");
		});
	}

	@Test
	void testProcessNewVisitFormDoesNotBindId() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID).param("id", "42")
				.param("description", "Visit Description"))
			.andExpect(status().is3xxRedirection());

		assertThat(pet.getVisits()).singleElement().satisfies(visit -> {
			assertThat(visit.getId()).isNull();
			assertThat(visit.isNew()).isTrue();
		});
	}

	@Test
	void testProcessNewVisitFormHasErrorsDoesNotSave() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID).param("description",
					""))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasFieldErrors("visit", "description"))
			.andExpect(view().name("pets/createOrUpdateVisitForm"));

		verify(this.owners, never()).save(any(Owner.class));
	}

}
