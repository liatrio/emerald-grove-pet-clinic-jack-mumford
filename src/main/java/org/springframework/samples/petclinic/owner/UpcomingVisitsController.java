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

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for displaying upcoming visits scheduled at the clinic.
 *
 * @author Claude Sonnet 4.5
 */
@Controller
@RequestMapping("/visits")
public class UpcomingVisitsController {

	private final VisitRepository visitRepository;

	private final PetTypeRepository petTypeRepository;

	public UpcomingVisitsController(VisitRepository visitRepository, PetTypeRepository petTypeRepository) {
		this.visitRepository = visitRepository;
		this.petTypeRepository = petTypeRepository;
	}

	/**
	 * Display all upcoming visits (visits with dates >= today) in chronological order.
	 * Supports optional filtering by date range, pet type, and owner name.
	 * @param fromDate the minimum date for visits (optional, defaults to today)
	 * @param toDate the maximum date for visits (optional)
	 * @param petType the pet type to filter by (optional)
	 * @param ownerLastName the owner last name to search for (optional)
	 * @param model the Spring MVC model
	 * @return the view name
	 */
	@GetMapping("/upcoming")
	public String showUpcomingVisits(
			@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate,
			@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toDate,
			@RequestParam(required = false) String petType, @RequestParam(required = false) String ownerLastName,
			Model model) {

		// Default fromDate to today if not provided
		LocalDate from = fromDate != null ? fromDate : LocalDate.now();

		// Use filtered query if any filter is provided
		List<Visit> visits;
		if (toDate != null || petType != null || ownerLastName != null) {
			visits = this.visitRepository.findUpcomingVisitsWithFilters(from, toDate, petType, ownerLastName);
		}
		else {
			visits = this.visitRepository.findByDateGreaterThanEqualOrderByDateAsc(from);
		}

		// Add visits and pet types to model
		model.addAttribute("visits", visits);
		model.addAttribute("petTypes", this.petTypeRepository.findAll());

		return "visits/upcomingVisits";
	}

}
