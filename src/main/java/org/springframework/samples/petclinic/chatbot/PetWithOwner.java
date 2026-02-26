package org.springframework.samples.petclinic.chatbot;

import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.Pet;

/**
 * A simple wrapper class that pairs a Pet with its Owner. Used by PetQueryService to
 * return both pet and owner information together.
 */
public class PetWithOwner {

	private final Pet pet;

	private final Owner owner;

	/**
	 * Creates a new PetWithOwner instance.
	 * @param pet the pet
	 * @param owner the owner of the pet
	 */
	public PetWithOwner(Pet pet, Owner owner) {
		this.pet = pet;
		this.owner = owner;
	}

	/**
	 * Gets the pet.
	 * @return the pet
	 */
	public Pet getPet() {
		return pet;
	}

	/**
	 * Gets the owner.
	 * @return the owner
	 */
	public Owner getOwner() {
		return owner;
	}

}
