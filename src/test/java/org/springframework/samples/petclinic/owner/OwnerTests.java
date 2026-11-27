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
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Owner}
 */
class OwnerTests {

	private Owner owner;

	private Pet leo;

	@BeforeEach
	void setup() {
		owner = new Owner();
		leo = new Pet();
		leo.setName("Leo");
		owner.addPet(leo);
		leo.setId(1);
	}

	private Pet newPet(String name) {
		Pet pet = new Pet();
		pet.setName(name);
		return pet;
	}

	@Test
	void shouldAddNewPet() {
		Pet basil = newPet("Basil");

		owner.addPet(basil);

		assertThat(owner.getPets()).containsExactly(leo, basil);
	}

	@Test
	void shouldNotAddPetThatAlreadyHasAnId() {
		Pet persisted = newPet("Rosy");
		persisted.setId(5);

		owner.addPet(persisted);

		assertThat(owner.getPets()).containsExactly(leo);
	}

	@Test
	void shouldFindPetByNameIgnoringCase() {
		assertThat(owner.getPet("Leo")).isSameAs(leo);
		assertThat(owner.getPet("lEO")).isSameAs(leo);
	}

	@Test
	void shouldReturnNullForUnknownPetName() {
		assertThat(owner.getPet("Unknown")).isNull();
		assertThat(owner.getPet((String) null)).isNull();
	}

	@Test
	void shouldIgnorePetsWithoutNameWhenSearchingByName() {
		owner.addPet(new Pet());

		assertThat(owner.getPet("Leo")).isSameAs(leo);
	}

	@Test
	void shouldReturnNewPetByNameWhenNotIgnoringNew() {
		Pet basil = newPet("Basil");
		owner.addPet(basil);

		assertThat(owner.getPet("Basil", false)).isSameAs(basil);
	}

	@Test
	void shouldIgnoreNewPetByNameWhenIgnoringNew() {
		Pet basil = newPet("Basil");
		owner.addPet(basil);

		assertThat(owner.getPet("Basil", true)).isNull();
		assertThat(owner.getPet("Leo", true)).isSameAs(leo);
	}

	@Test
	void shouldFindPetById() {
		assertThat(owner.getPet(1)).isSameAs(leo);
	}

	@Test
	void shouldReturnNullForUnknownPetId() {
		assertThat(owner.getPet(99)).isNull();
	}

	@Test
	void shouldIgnoreNewPetsWhenSearchingById() {
		owner.addPet(newPet("Basil"));

		assertThat(owner.getPet((Integer) null)).isNull();
	}

	@Test
	void shouldAddVisitToPet() {
		Visit visit = new Visit();
		visit.setDescription("rabies shot");

		owner.addVisit(1, visit);

		assertThat(leo.getVisits()).containsExactly(visit);
	}

	@Test
	void shouldRejectVisitForUnknownPetId() {
		assertThatIllegalArgumentException().isThrownBy(() -> owner.addVisit(99, new Visit()))
			.withMessage("Invalid Pet identifier!");
		assertThat(leo.getVisits()).isEmpty();
	}

	@Test
	void shouldRejectVisitWithNullPetId() {
		assertThatIllegalArgumentException().isThrownBy(() -> owner.addVisit(null, new Visit()))
			.withMessage("Pet identifier must not be null!");
	}

	@Test
	void shouldRejectNullVisit() {
		assertThatIllegalArgumentException().isThrownBy(() -> owner.addVisit(1, null))
			.withMessage("Visit must not be null!");
		assertThat(leo.getVisits()).isEmpty();
	}

	@Test
	void shouldIncludeFieldsInToString() {
		owner.setId(7);
		owner.setFirstName("George");
		owner.setLastName("Franklin");
		owner.setAddress("110 W. Liberty St.");
		owner.setCity("Madison");
		owner.setTelephone("6085551023");

		assertThat(owner.toString()).contains("id = 7", "new = false", "lastName = 'Franklin'", "firstName = 'George'",
				"address = '110 W. Liberty St.'", "city = 'Madison'", "telephone = '6085551023'");
	}

}
