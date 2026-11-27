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

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Pet}
 */
class PetTests {

	@Test
	void shouldHaveNoVisitsInitially() {
		Pet pet = new Pet();

		assertThat(pet.getVisits()).isEmpty();
	}

	@Test
	void shouldAddVisitsInInsertionOrder() {
		Pet pet = new Pet();
		Visit first = new Visit();
		first.setDescription("first");
		Visit second = new Visit();
		second.setDescription("second");

		pet.addVisit(first);
		pet.addVisit(second);

		assertThat(pet.getVisits()).containsExactly(first, second);
	}

	@Test
	void shouldNotDuplicateSameVisitInstance() {
		Pet pet = new Pet();
		Visit visit = new Visit();

		pet.addVisit(visit);
		pet.addVisit(visit);

		assertThat(pet.getVisits()).hasSize(1);
	}

	@Test
	void shouldDefaultVisitDateToToday() {
		Visit visit = new Visit();

		assertThat(visit.getDate()).isEqualTo(LocalDate.now());
	}

	@Test
	void shouldUseNameAsToString() {
		Pet pet = new Pet();
		assertThat(pet).hasToString("<null>");

		pet.setName("Leo");
		assertThat(pet).hasToString("Leo");
	}

}
