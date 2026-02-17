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

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository class for <code>Owner</code> domain objects. All method names are compliant
 * with Spring Data naming conventions so this interface can easily be extended for Spring
 * Data. See:
 * https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.query-methods.query-creation
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 * @author Wick Dynex
 */
public interface OwnerRepository extends JpaRepository<Owner, Integer> {

	/**
	 * Retrieve {@link Owner}s from the data store by last name, returning all owners
	 * whose last name <i>starts</i> with the given name.
	 * @param lastName Value to search for
	 * @return a Collection of matching {@link Owner}s (or an empty Collection if none
	 * found)
	 */
	Page<Owner> findByLastNameStartingWith(String lastName, Pageable pageable);

	/**
	 * Retrieve all {@link Owner}s from the data store by last name (unpaginated),
	 * returning all owners whose last name <i>starts</i> with the given name. This method
	 * is intended for CSV export where all results are needed.
	 * @param lastName Value to search for
	 * @return a Collection of matching {@link Owner}s (or an empty Collection if none
	 * found)
	 */
	List<Owner> findByLastNameStartingWith(String lastName);

	/**
	 * Retrieve an {@link Owner} from the data store by id.
	 * <p>
	 * This method returns an {@link Optional} containing the {@link Owner} if found. If
	 * no {@link Owner} is found with the provided id, it will return an empty
	 * {@link Optional}.
	 * </p>
	 * @param id the id to search for
	 * @return an {@link Optional} containing the {@link Owner} if found, or an empty
	 * {@link Optional} if not found.
	 * @throws IllegalArgumentException if the id is null (assuming null is not a valid
	 * input for id)
	 */
	Optional<Owner> findById(Integer id);

	/**
	 * Find owners by first name, last name, and telephone (case-insensitive). Used for
	 * duplicate detection during owner creation.
	 * @param firstName the owner's first name (case-insensitive)
	 * @param lastName the owner's last name (case-insensitive)
	 * @param telephone the owner's telephone number
	 * @return list of owners matching all three fields (empty if none found)
	 */
	List<Owner> findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndTelephone(String firstName, String lastName,
			String telephone);

	/**
	 * Find owners by multiple criteria: last name, telephone, and/or city. All parameters
	 * are optional (can be null).
	 * @param lastName the last name to search for (starts with, case-insensitive), can be
	 * null
	 * @param telephone the telephone to search for (starts with), can be null
	 * @param city the city to search for (exact match, case-insensitive), can be null
	 * @param pageable pagination information
	 * @return a page of matching owners
	 */
	@Query("SELECT o FROM Owner o WHERE " + "(?1 IS NULL OR LOWER(o.lastName) LIKE LOWER(CONCAT(?1, '%'))) AND "
			+ "(?2 IS NULL OR o.telephone LIKE CONCAT(?2, '%')) AND " + "(?3 IS NULL OR LOWER(o.city) = LOWER(?3))")
	Page<Owner> findByMultipleCriteria(String lastName, String telephone, String city, Pageable pageable);

}
