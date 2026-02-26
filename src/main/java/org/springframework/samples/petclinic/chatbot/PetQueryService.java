package org.springframework.samples.petclinic.chatbot;

import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Service for querying pet information from the database. Provides methods to search for
 * pets by name or owner, and format pet information for display. All inputs are sanitized
 * to prevent SQL injection attacks.
 */
@Service
public class PetQueryService {

	private final OwnerRepository ownerRepository;

	// Pattern to detect potentially dangerous SQL injection patterns.
	// Note: Spring Data JPA uses parameterized queries, so SQL injection via
	// parameters is not possible. This is a defense-in-depth measure only.
	private static final Pattern DANGEROUS_CHARS = Pattern.compile(
			"[';\"\\\\]|--|((?i)(\\bdrop\\b|\\bdelete\\b|\\binsert\\b|\\bupdate\\b|\\bunion\\b|\\bselect\\b|\\bexec\\b|\\bexecute\\b))");

	/**
	 * Creates a new pet query service.
	 * @param ownerRepository the owner repository for database access
	 */
	public PetQueryService(OwnerRepository ownerRepository) {
		this.ownerRepository = ownerRepository;
	}

	/**
	 * Finds a pet by name (case-insensitive). Returns the first matching pet if multiple
	 * pets have the same name.
	 * @param petName the name of the pet to find
	 * @return an Optional containing the pet and its owner if found, empty otherwise
	 */
	public Optional<PetWithOwner> findPetByName(String petName) {
		String sanitizedName = sanitizeInput(petName);
		if (sanitizedName == null || sanitizedName.isEmpty()) {
			return Optional.empty();
		}

		List<Owner> owners = ownerRepository.findAll();
		for (Owner owner : owners) {
			for (Pet pet : owner.getPets()) {
				if (pet.getName() != null && pet.getName().equalsIgnoreCase(sanitizedName)) {
					return Optional.of(new PetWithOwner(pet, owner));
				}
			}
		}
		return Optional.empty();
	}

	/**
	 * Finds all pets owned by an owner with the given last name (case-insensitive).
	 * @param ownerLastName the last name of the owner
	 * @return a list of pets with their owner, empty list if none found
	 */
	public List<PetWithOwner> findPetsByOwnerLastName(String ownerLastName) {
		String sanitizedName = sanitizeInput(ownerLastName);
		if (sanitizedName == null || sanitizedName.isEmpty()) {
			return new ArrayList<>();
		}

		List<PetWithOwner> results = new ArrayList<>();
		List<Owner> owners = ownerRepository.findAll();

		for (Owner owner : owners) {
			if (owner.getLastName() != null && owner.getLastName().equalsIgnoreCase(sanitizedName)) {
				for (Pet pet : owner.getPets()) {
					results.add(new PetWithOwner(pet, owner));
				}
			}
		}
		return results;
	}

	/**
	 * Finds all pets in the database.
	 * @return a list of all pets with their owners
	 */
	public List<PetWithOwner> findAllPets() {
		List<PetWithOwner> results = new ArrayList<>();
		List<Owner> owners = ownerRepository.findAll();

		for (Owner owner : owners) {
			for (Pet pet : owner.getPets()) {
				results.add(new PetWithOwner(pet, owner));
			}
		}
		return results;
	}

	/**
	 * Formats pet information into a human-readable string.
	 * @param pet the pet to format
	 * @param owner the owner of the pet
	 * @return a formatted string with pet and owner information
	 */
	public String formatPetInfo(Pet pet, Owner owner) {
		StringBuilder info = new StringBuilder();
		info.append(pet.getName()).append(" is a ").append(pet.getType().getName());

		if (pet.getBirthDate() != null) {
			info.append(", born on ").append(pet.getBirthDate());
		}

		info.append(", owned by ").append(owner.getFirstName()).append(" ").append(owner.getLastName());

		return info.toString();
	}

	/**
	 * Sanitizes user input to prevent SQL injection and other attacks. Removes
	 * potentially dangerous characters and SQL keywords, trims whitespace.
	 * @param input the input string to sanitize
	 * @return the sanitized string, or null if input is null
	 */
	private String sanitizeInput(String input) {
		if (input == null) {
			return null;
		}

		// Trim whitespace
		String sanitized = input.trim();

		// Check for SQL injection patterns
		if (DANGEROUS_CHARS.matcher(sanitized).find()) {
			// If dangerous patterns found, return empty string to prevent match
			return "";
		}

		return sanitized;
	}

}
