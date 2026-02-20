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
package org.springframework.samples.petclinic.system;

import org.springframework.boot.CommandLineRunner;
import org.springframework.samples.petclinic.owner.PetType;
import org.springframework.samples.petclinic.owner.PetTypeRepository;
import org.springframework.stereotype.Component;

/**
 * Initializes essential reference data if not present in the database. This ensures pet
 * types and other reference data are available even when using a file-based database that
 * doesn't run initialization scripts.
 */
@Component
public class DataInitializer implements CommandLineRunner {

	private final PetTypeRepository petTypeRepository;

	public DataInitializer(PetTypeRepository petTypeRepository) {
		this.petTypeRepository = petTypeRepository;
	}

	@Override
	public void run(String... args) {
		// Initialize pet types if none exist
		if (petTypeRepository.findPetTypes().isEmpty()) {
			String[] petTypeNames = { "cat", "dog", "lizard", "snake", "bird", "hamster" };
			for (String typeName : petTypeNames) {
				PetType type = new PetType();
				type.setName(typeName);
				petTypeRepository.save(type);
			}
		}
	}

}
