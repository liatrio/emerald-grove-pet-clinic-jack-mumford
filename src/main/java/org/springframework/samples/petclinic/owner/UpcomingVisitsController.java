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

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for displaying upcoming visits scheduled at the clinic.
 *
 * @author Claude Sonnet 4.5
 */
@Controller
@RequestMapping("/visits")
public class UpcomingVisitsController {

	private final VisitRepository visitRepository;

	public UpcomingVisitsController(VisitRepository visitRepository) {
		this.visitRepository = visitRepository;
	}

	/**
	 * Display all upcoming visits (visits with dates >= today) in chronological order.
	 * @param model the Spring MVC model
	 * @return the view name
	 */
	@GetMapping("/upcoming")
	public String showUpcomingVisits(Model model) {
		LocalDate fromDate = LocalDate.now();
		List<Visit> visits = this.visitRepository.findByDateGreaterThanEqualOrderByDateAsc(fromDate);
		model.addAttribute("visits", visits);
		return "visits/upcomingVisits";
	}

}
