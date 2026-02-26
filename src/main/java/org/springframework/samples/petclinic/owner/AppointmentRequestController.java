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

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.validation.Valid;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for handling appointment requests from pet owners.
 *
 * @author Claude Sonnet 4.5
 */
@Controller
class AppointmentRequestController {

	private final OwnerRepository owners;

	private final VisitValidator visitValidator;

	public AppointmentRequestController(OwnerRepository owners, VisitValidator visitValidator) {
		this.owners = owners;
		this.visitValidator = visitValidator;
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@InitBinder("visit")
	public void initVisitBinder(WebDataBinder dataBinder) {
		dataBinder.addValidators(visitValidator);
	}

	/**
	 * Load pet and owner data for the appointment request form. Creates a new Visit
	 * object with PENDING status by default.
	 * @param ownerId the owner's ID
	 * @param petId the pet's ID
	 * @param model the model to populate
	 * @return Visit object with PENDING status
	 */
	@ModelAttribute("visit")
	public Visit loadPetWithVisit(@PathVariable("ownerId") int ownerId, @PathVariable("petId") int petId,
			Map<String, Object> model) {
		Optional<Owner> optionalOwner = owners.findById(ownerId);
		Owner owner = optionalOwner.orElseThrow(() -> new IllegalArgumentException(
				"Owner not found with id: " + ownerId + ". Please ensure the ID is correct"));

		Pet pet = owner.getPet(petId);
		if (pet == null) {
			throw new IllegalArgumentException(
					"Pet with id " + petId + " not found for owner with id " + ownerId + ".");
		}
		model.put("pet", pet);
		model.put("owner", owner);

		Visit visit = new Visit();
		visit.setStatus(VisitStatus.PENDING);
		pet.addVisit(visit);
		return visit;
	}

	/**
	 * Show the appointment request form.
	 * @return the view name for the appointment request form
	 */
	@GetMapping("/owners/{ownerId}/pets/{petId}/appointments/request")
	public String showAppointmentRequestForm() {
		return "appointments/appointmentRequestForm";
	}

	/**
	 * Process the appointment request form submission.
	 * @param owner the owner (loaded via @ModelAttribute)
	 * @param petId the pet's ID
	 * @param visit the visit object bound from form data
	 * @param result the binding result for validation
	 * @param redirectAttributes for flash messages
	 * @return redirect to owner details page on success, or back to form on error
	 */
	@PostMapping("/owners/{ownerId}/pets/{petId}/appointments/request")
	public String processAppointmentRequest(@ModelAttribute Owner owner, @PathVariable int petId, @Valid Visit visit,
			BindingResult result, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			return "appointments/appointmentRequestForm";
		}

		// Ensure status is PENDING for new appointment requests
		visit.setStatus(VisitStatus.PENDING);

		owner.addVisit(petId, visit);
		this.owners.save(owner);
		redirectAttributes.addFlashAttribute("message", "Your appointment request has been submitted");
		return "redirect:/owners/{ownerId}";
	}

}
