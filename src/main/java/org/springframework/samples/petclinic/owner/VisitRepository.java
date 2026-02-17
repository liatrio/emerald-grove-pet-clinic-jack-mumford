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

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/**
 * Repository interface for {@link Visit} domain objects. All method names are compliant
 * with Spring Data naming conventions so this interface can easily be extended for Spring
 * Data.
 *
 * @author Claude Sonnet 4.5
 */
public interface VisitRepository extends Repository<Visit, Integer> {

	/**
	 * Retrieve all {@link Visit}s from the data store with dates greater than or equal to
	 * the specified date, ordered by date in ascending order. Uses JOIN FETCH to eagerly
	 * load Pet and Owner relationships to prevent N+1 query issues.
	 * @param fromDate the minimum date for visits to retrieve
	 * @return a List of {@link Visit}s matching the criteria (or an empty List if none
	 * found)
	 */
	@Query("SELECT v FROM Visit v JOIN FETCH v.pet p JOIN FETCH p.owner WHERE v.date >= :fromDate ORDER BY v.date ASC")
	List<Visit> findByDateGreaterThanEqualOrderByDateAsc(@Param("fromDate") LocalDate fromDate);

	/**
	 * Retrieve all {@link Visit}s from the data store with dates within the specified
	 * range (inclusive), ordered by date in ascending order. Uses JOIN FETCH to eagerly
	 * load Pet and Owner relationships to prevent N+1 query issues.
	 * @param fromDate the minimum date for visits to retrieve
	 * @param toDate the maximum date for visits to retrieve
	 * @return a List of {@link Visit}s matching the criteria (or an empty List if none
	 * found)
	 */
	@Query("SELECT v FROM Visit v JOIN FETCH v.pet p JOIN FETCH p.owner WHERE v.date BETWEEN :fromDate AND :toDate ORDER BY v.date ASC")
	List<Visit> findByDateBetweenOrderByDateAsc(@Param("fromDate") LocalDate fromDate,
			@Param("toDate") LocalDate toDate);

	/**
	 * Retrieve all {@link Visit}s from the data store matching the specified filter
	 * criteria. All filter parameters are optional (null values are ignored). Uses JOIN
	 * FETCH to eagerly load Pet and Owner relationships to prevent N+1 query issues.
	 * @param fromDate the minimum date for visits to retrieve (required)
	 * @param toDate the maximum date for visits to retrieve (optional, null for no upper
	 * bound)
	 * @param petType the pet type name to filter by (optional, null for all types,
	 * case-insensitive)
	 * @param ownerLastName the owner last name to search for (optional, null for all
	 * owners, case-insensitive partial match)
	 * @return a List of {@link Visit}s matching the criteria (or an empty List if none
	 * found)
	 */
	@Query("SELECT v FROM Visit v JOIN FETCH v.pet p JOIN FETCH p.owner o JOIN FETCH p.type t "
			+ "WHERE v.date >= :fromDate " + "AND (:toDate IS NULL OR v.date <= :toDate) "
			+ "AND (:petType IS NULL OR LOWER(t.name) = LOWER(:petType)) "
			+ "AND (:ownerLastName IS NULL OR LOWER(o.lastName) LIKE LOWER(CONCAT('%', :ownerLastName, '%'))) "
			+ "ORDER BY v.date ASC")
	List<Visit> findUpcomingVisitsWithFilters(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate,
			@Param("petType") String petType, @Param("ownerLastName") String ownerLastName);

}
