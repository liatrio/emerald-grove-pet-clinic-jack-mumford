package org.springframework.samples.petclinic.chatbot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * Tests for {@link PetQueryService}.
 */
@ExtendWith(MockitoExtension.class)
class PetQueryServiceTests {

	@Mock
	private OwnerRepository ownerRepository;

	private PetQueryService petQueryService;

	private Owner testOwner1;

	private Owner testOwner2;

	private Pet testPet1;

	private Pet testPet2;

	private Pet testPet3;

	@BeforeEach
	void setUp() {
		petQueryService = new PetQueryService(ownerRepository);

		// Setup test owner 1 with pet "Leo"
		testOwner1 = new Owner();
		testOwner1.setId(1);
		testOwner1.setFirstName("George");
		testOwner1.setLastName("Franklin");
		testOwner1.setAddress("110 W. Liberty St.");
		testOwner1.setCity("Madison");
		testOwner1.setTelephone("6085551023");

		PetType catType = new PetType();
		catType.setId(1);
		catType.setName("cat");

		testPet1 = new Pet();
		testPet1.setId(1);
		testPet1.setName("Leo");
		testPet1.setType(catType);
		testPet1.setBirthDate(LocalDate.of(2010, 9, 7));
		testPet1.setOwner(testOwner1);
		testOwner1.getPets().add(testPet1);

		// Setup test owner 2 with pets "Max" and "Bella"
		testOwner2 = new Owner();
		testOwner2.setId(2);
		testOwner2.setFirstName("Betty");
		testOwner2.setLastName("Davis");
		testOwner2.setAddress("638 Cardinal Ave.");
		testOwner2.setCity("Sun Prairie");
		testOwner2.setTelephone("6085551749");

		PetType dogType = new PetType();
		dogType.setId(2);
		dogType.setName("dog");

		testPet2 = new Pet();
		testPet2.setId(2);
		testPet2.setName("Max");
		testPet2.setType(dogType);
		testPet2.setBirthDate(LocalDate.of(2012, 8, 6));
		testPet2.setOwner(testOwner2);
		testOwner2.getPets().add(testPet2);

		testPet3 = new Pet();
		testPet3.setId(3);
		testPet3.setName("Bella");
		testPet3.setType(dogType);
		testPet3.setBirthDate(LocalDate.of(2015, 2, 24));
		testPet3.setOwner(testOwner2);
		testOwner2.getPets().add(testPet3);
	}

	@Test
	void testFindPetByName_Found() {
		// Arrange
		List<Owner> owners = List.of(testOwner1, testOwner2);
		given(ownerRepository.findAll()).willReturn(owners);

		// Act
		Optional<PetWithOwner> result = petQueryService.findPetByName("Leo");

		// Assert
		assertThat(result).isPresent();
		assertThat(result.get().getPet().getName()).isEqualTo("Leo");
		assertThat(result.get().getPet().getType().getName()).isEqualTo("cat");
		assertThat(result.get().getOwner().getFirstName()).isEqualTo("George");
		assertThat(result.get().getOwner().getLastName()).isEqualTo("Franklin");
	}

	@Test
	void testFindPetByName_CaseInsensitive() {
		// Arrange
		List<Owner> owners = List.of(testOwner1, testOwner2);
		given(ownerRepository.findAll()).willReturn(owners);

		// Act
		Optional<PetWithOwner> resultLowerCase = petQueryService.findPetByName("leo");
		Optional<PetWithOwner> resultUpperCase = petQueryService.findPetByName("LEO");
		Optional<PetWithOwner> resultMixedCase = petQueryService.findPetByName("LeO");

		// Assert
		assertThat(resultLowerCase).isPresent();
		assertThat(resultLowerCase.get().getPet().getName()).isEqualTo("Leo");
		assertThat(resultUpperCase).isPresent();
		assertThat(resultUpperCase.get().getPet().getName()).isEqualTo("Leo");
		assertThat(resultMixedCase).isPresent();
		assertThat(resultMixedCase.get().getPet().getName()).isEqualTo("Leo");
	}

	@Test
	void testFindPetByName_NotFound() {
		// Arrange
		List<Owner> owners = List.of(testOwner1, testOwner2);
		given(ownerRepository.findAll()).willReturn(owners);

		// Act
		Optional<PetWithOwner> result = petQueryService.findPetByName("NonExistentPet");

		// Assert
		assertThat(result).isEmpty();
	}

	@Test
	void testFindPetByName_EmptyDatabase() {
		// Arrange
		given(ownerRepository.findAll()).willReturn(new ArrayList<>());

		// Act
		Optional<PetWithOwner> result = petQueryService.findPetByName("Leo");

		// Assert
		assertThat(result).isEmpty();
	}

	@Test
	void testFindPetByName_NullName() {
		// Arrange - no stubbing needed as method returns early

		// Act
		Optional<PetWithOwner> result = petQueryService.findPetByName(null);

		// Assert
		assertThat(result).isEmpty();
	}

	@Test
	void testFindPetByName_EmptyName() {
		// Arrange - no stubbing needed as method returns early

		// Act
		Optional<PetWithOwner> result = petQueryService.findPetByName("");

		// Assert
		assertThat(result).isEmpty();
	}

	@Test
	void testFindPetsByOwnerLastName_Found() {
		// Arrange
		List<Owner> owners = List.of(testOwner1, testOwner2);
		given(ownerRepository.findAll()).willReturn(owners);

		// Act
		List<PetWithOwner> results = petQueryService.findPetsByOwnerLastName("Davis");

		// Assert
		assertThat(results).hasSize(2);
		assertThat(results.get(0).getPet().getName()).isEqualTo("Max");
		assertThat(results.get(1).getPet().getName()).isEqualTo("Bella");
		assertThat(results.get(0).getOwner().getLastName()).isEqualTo("Davis");
	}

	@Test
	void testFindPetsByOwnerLastName_CaseInsensitive() {
		// Arrange
		List<Owner> owners = List.of(testOwner1, testOwner2);
		given(ownerRepository.findAll()).willReturn(owners);

		// Act
		List<PetWithOwner> results = petQueryService.findPetsByOwnerLastName("davis");

		// Assert
		assertThat(results).hasSize(2);
		assertThat(results.get(0).getOwner().getLastName()).isEqualTo("Davis");
	}

	@Test
	void testFindPetsByOwnerLastName_NotFound() {
		// Arrange
		List<Owner> owners = List.of(testOwner1, testOwner2);
		given(ownerRepository.findAll()).willReturn(owners);

		// Act
		List<PetWithOwner> results = petQueryService.findPetsByOwnerLastName("NonExistent");

		// Assert
		assertThat(results).isEmpty();
	}

	@Test
	void testFindPetsByOwnerLastName_NullName() {
		// Arrange - no stubbing needed as method returns early

		// Act
		List<PetWithOwner> results = petQueryService.findPetsByOwnerLastName(null);

		// Assert
		assertThat(results).isEmpty();
	}

	@Test
	void testFormatPetInfo_Complete() {
		// Act
		String result = petQueryService.formatPetInfo(testPet1, testOwner1);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result).contains("Leo");
		assertThat(result).contains("cat");
		assertThat(result).contains("2010-09-07");
		assertThat(result).contains("George Franklin");
	}

	@Test
	void testFormatPetInfo_WithoutBirthDate() {
		// Arrange
		Pet petWithoutBirthDate = new Pet();
		petWithoutBirthDate.setName("Fluffy");
		PetType hamsterType = new PetType();
		hamsterType.setName("hamster");
		petWithoutBirthDate.setType(hamsterType);

		// Act
		String result = petQueryService.formatPetInfo(petWithoutBirthDate, testOwner1);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result).contains("Fluffy");
		assertThat(result).contains("hamster");
		assertThat(result).contains("George Franklin");
	}

	@Test
	void testInputSanitization_SQLInjection() {
		// Arrange
		List<Owner> owners = List.of(testOwner1);
		given(ownerRepository.findAll()).willReturn(owners);

		// Act - try SQL injection patterns
		Optional<PetWithOwner> result1 = petQueryService.findPetByName("Leo'; DROP TABLE pets; --");
		Optional<PetWithOwner> result2 = petQueryService.findPetByName("Leo OR 1=1");

		// Assert - sanitization should prevent matches or return empty
		assertThat(result1).isEmpty();
		assertThat(result2).isEmpty();
	}

	@Test
	void testInputSanitization_SpecialCharacters() {
		// Arrange - no stubbing needed as sanitization prevents query

		// Act - try special characters
		Optional<PetWithOwner> result = petQueryService.findPetByName("<script>alert('xss')</script>");

		// Assert
		assertThat(result).isEmpty();
	}

	@Test
	void testInputSanitization_WhitespaceHandling() {
		// Arrange
		List<Owner> owners = List.of(testOwner1);
		given(ownerRepository.findAll()).willReturn(owners);

		// Act - try with leading/trailing whitespace
		Optional<PetWithOwner> result = petQueryService.findPetByName("  Leo  ");

		// Assert - should still find the pet after trimming
		assertThat(result).isPresent();
		assertThat(result.get().getPet().getName()).isEqualTo("Leo");
	}

	@Test
	void testFindAllPets() {
		// Arrange
		List<Owner> owners = List.of(testOwner1, testOwner2);
		given(ownerRepository.findAll()).willReturn(owners);

		// Act
		List<PetWithOwner> results = petQueryService.findAllPets();

		// Assert
		assertThat(results).hasSize(3);
		assertThat(results).extracting(pw -> pw.getPet().getName()).containsExactlyInAnyOrder("Leo", "Max", "Bella");
	}

	@Test
	void testFindAllPets_EmptyDatabase() {
		// Arrange
		given(ownerRepository.findAll()).willReturn(new ArrayList<>());

		// Act
		List<PetWithOwner> results = petQueryService.findAllPets();

		// Assert
		assertThat(results).isEmpty();
	}

}
