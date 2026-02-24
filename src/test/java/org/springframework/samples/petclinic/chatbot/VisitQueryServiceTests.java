package org.springframework.samples.petclinic.chatbot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.samples.petclinic.owner.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

/**
 * Unit tests for {@link VisitQueryService}. Tests visit query functionality including
 * finding upcoming visits, filtering by date range, searching by pet name, and formatting
 * visit information for display.
 */
@ExtendWith(MockitoExtension.class)
class VisitQueryServiceTests {

	@Mock
	private VisitRepository visitRepository;

	@InjectMocks
	private VisitQueryService visitQueryService;

	private Visit testVisit;

	private Pet testPet;

	private Owner testOwner;

	private PetType testPetType;

	@BeforeEach
	void setUp() {
		// Set up test data
		testOwner = new Owner();
		testOwner.setId(1);
		testOwner.setFirstName("John");
		testOwner.setLastName("Doe");

		testPetType = new PetType();
		testPetType.setId(1);
		testPetType.setName("cat");

		testPet = new Pet();
		testPet.setId(1);
		testPet.setName("Leo");
		testPet.setType(testPetType);
		testPet.setOwner(testOwner);

		testVisit = new Visit();
		testVisit.setId(1);
		testVisit.setDate(LocalDate.now().plusDays(7));
		testVisit.setAppointmentTime(LocalDateTime.now().plusDays(7).withHour(10).withMinute(0));
		testVisit.setVisitType(VisitType.CHECKUP);
		testVisit.setStatus(VisitStatus.SCHEDULED);
		testVisit.setDescription("Regular checkup");
		testVisit.setPet(testPet);
	}

	@Test
	void testFindUpcomingVisits() {
		// Arrange
		given(visitRepository.findByDateGreaterThanEqualOrderByDateAsc(any(LocalDate.class)))
			.willReturn(Arrays.asList(testVisit));

		// Act
		List<Visit> visits = visitQueryService.findUpcomingVisits();

		// Assert
		assertThat(visits).isNotNull();
		assertThat(visits).hasSize(1);
		assertThat(visits.get(0)).isEqualTo(testVisit);
	}

	@Test
	void testFindUpcomingVisitsEmpty() {
		// Arrange
		given(visitRepository.findByDateGreaterThanEqualOrderByDateAsc(any(LocalDate.class)))
			.willReturn(Collections.emptyList());

		// Act
		List<Visit> visits = visitQueryService.findUpcomingVisits();

		// Assert
		assertThat(visits).isNotNull();
		assertThat(visits).isEmpty();
	}

	@Test
	void testFindVisitsByDateRange() {
		// Arrange
		LocalDate start = LocalDate.now();
		LocalDate end = LocalDate.now().plusDays(30);
		given(visitRepository.findByDateBetweenOrderByDateAsc(start, end)).willReturn(Arrays.asList(testVisit));

		// Act
		List<Visit> visits = visitQueryService.findVisitsByDateRange(start, end);

		// Assert
		assertThat(visits).isNotNull();
		assertThat(visits).hasSize(1);
		assertThat(visits.get(0)).isEqualTo(testVisit);
	}

	@Test
	void testFindVisitsByDateRangeEmpty() {
		// Arrange
		LocalDate start = LocalDate.now().plusDays(60);
		LocalDate end = LocalDate.now().plusDays(90);
		given(visitRepository.findByDateBetweenOrderByDateAsc(start, end)).willReturn(Collections.emptyList());

		// Act
		List<Visit> visits = visitQueryService.findVisitsByDateRange(start, end);

		// Assert
		assertThat(visits).isNotNull();
		assertThat(visits).isEmpty();
	}

	@Test
	void testFindVisitsByPetName() {
		// Arrange
		given(visitRepository.findByDateGreaterThanEqualOrderByDateAsc(any(LocalDate.class)))
			.willReturn(Arrays.asList(testVisit));

		// Act
		List<Visit> visits = visitQueryService.findVisitsByPetName("Leo");

		// Assert
		assertThat(visits).isNotNull();
		assertThat(visits).hasSize(1);
		assertThat(visits.get(0).getPet().getName()).isEqualTo("Leo");
	}

	@Test
	void testFindVisitsByPetNameCaseInsensitive() {
		// Arrange
		given(visitRepository.findByDateGreaterThanEqualOrderByDateAsc(any(LocalDate.class)))
			.willReturn(Arrays.asList(testVisit));

		// Act
		List<Visit> visits = visitQueryService.findVisitsByPetName("leo");

		// Assert
		assertThat(visits).isNotNull();
		assertThat(visits).hasSize(1);
		assertThat(visits.get(0).getPet().getName()).isEqualTo("Leo");
	}

	@Test
	void testFindVisitsByPetNameNoMatch() {
		// Arrange
		given(visitRepository.findByDateGreaterThanEqualOrderByDateAsc(any(LocalDate.class)))
			.willReturn(Arrays.asList(testVisit));

		// Act
		List<Visit> visits = visitQueryService.findVisitsByPetName("Max");

		// Assert
		assertThat(visits).isNotNull();
		assertThat(visits).isEmpty();
	}

	@Test
	void testFindVisitsByStatus() {
		// Arrange
		given(visitRepository.findByStatusOrderByDateAsc(VisitStatus.SCHEDULED)).willReturn(Arrays.asList(testVisit));

		// Act
		List<Visit> visits = visitQueryService.findVisitsByStatus(VisitStatus.SCHEDULED);

		// Assert
		assertThat(visits).isNotNull();
		assertThat(visits).hasSize(1);
		assertThat(visits.get(0).getStatus()).isEqualTo(VisitStatus.SCHEDULED);
	}

	@Test
	void testFindVisitsByStatusEmpty() {
		// Arrange
		given(visitRepository.findByStatusOrderByDateAsc(VisitStatus.CANCELLED)).willReturn(Collections.emptyList());

		// Act
		List<Visit> visits = visitQueryService.findVisitsByStatus(VisitStatus.CANCELLED);

		// Assert
		assertThat(visits).isNotNull();
		assertThat(visits).isEmpty();
	}

	@Test
	void testFormatVisitInfo() {
		// Arrange - visit already set up in setUp()
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
		String expectedDate = testVisit.getDate().format(dateFormatter);
		String expectedTime = testVisit.getAppointmentTime().format(timeFormatter);

		// Act
		String formatted = visitQueryService.formatVisitInfo(testVisit);

		// Assert
		assertThat(formatted).isNotNull();
		assertThat(formatted).contains("Leo");
		assertThat(formatted).contains("cat");
		assertThat(formatted).contains("Wellness Checkup");
		assertThat(formatted).contains(expectedDate);
		assertThat(formatted).contains(expectedTime);
		assertThat(formatted).contains("Scheduled");
	}

	@Test
	void testFormatVisitInfoWithoutAppointmentTime() {
		// Arrange
		testVisit.setAppointmentTime(null);

		// Act
		String formatted = visitQueryService.formatVisitInfo(testVisit);

		// Assert
		assertThat(formatted).isNotNull();
		assertThat(formatted).contains("Leo");
		assertThat(formatted).contains("cat");
		assertThat(formatted).doesNotContain(" at ");
	}

	@Test
	void testParseNaturalLanguageDateThisWeek() {
		// Act
		LocalDate result = visitQueryService.parseNaturalLanguageDate("this week");

		// Assert
		assertThat(result).isNotNull();
		assertThat(result).isEqualTo(LocalDate.now());
	}

	@Test
	void testParseNaturalLanguageDateNextWeek() {
		// Act
		LocalDate result = visitQueryService.parseNaturalLanguageDate("next week");

		// Assert
		assertThat(result).isNotNull();
		assertThat(result).isAfterOrEqualTo(LocalDate.now().plusDays(7));
	}

	@Test
	void testParseNaturalLanguageDateThisMonth() {
		// Act
		LocalDate result = visitQueryService.parseNaturalLanguageDate("this month");

		// Assert
		assertThat(result).isNotNull();
		assertThat(result).isEqualTo(LocalDate.now());
	}

	@Test
	void testParseNaturalLanguageDateNextMonth() {
		// Act
		LocalDate result = visitQueryService.parseNaturalLanguageDate("next month");

		// Assert
		assertThat(result).isNotNull();
		assertThat(result).isEqualTo(LocalDate.now().plusMonths(1).withDayOfMonth(1));
	}

	@Test
	void testParseNaturalLanguageDateToday() {
		// Act
		LocalDate result = visitQueryService.parseNaturalLanguageDate("today");

		// Assert
		assertThat(result).isNotNull();
		assertThat(result).isEqualTo(LocalDate.now());
	}

	@Test
	void testParseNaturalLanguageDateTomorrow() {
		// Act
		LocalDate result = visitQueryService.parseNaturalLanguageDate("tomorrow");

		// Assert
		assertThat(result).isNotNull();
		assertThat(result).isEqualTo(LocalDate.now().plusDays(1));
	}

	@Test
	void testParseNaturalLanguageDateMonthName() {
		// Act
		LocalDate result = visitQueryService.parseNaturalLanguageDate("March");

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getMonthValue()).isEqualTo(3);
		// Should be this year or next year depending on current month
		int expectedYear = LocalDate.now().getMonthValue() > 3 ? LocalDate.now().getYear() + 1
				: LocalDate.now().getYear();
		assertThat(result.getYear()).isEqualTo(expectedYear);
	}

	@Test
	void testParseNaturalLanguageDateUnrecognized() {
		// Act
		LocalDate result = visitQueryService.parseNaturalLanguageDate("some random text");

		// Assert
		assertThat(result).isEqualTo(LocalDate.now());
	}

	@Test
	void testParseNaturalLanguageDateNull() {
		// Act
		LocalDate result = visitQueryService.parseNaturalLanguageDate(null);

		// Assert
		assertThat(result).isEqualTo(LocalDate.now());
	}

	@Test
	void testParseNaturalLanguageDateEmpty() {
		// Act
		LocalDate result = visitQueryService.parseNaturalLanguageDate("");

		// Assert
		assertThat(result).isEqualTo(LocalDate.now());
	}

}
