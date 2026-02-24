package org.springframework.samples.petclinic.chatbot;

import org.springframework.samples.petclinic.owner.Visit;
import org.springframework.samples.petclinic.owner.VisitRepository;
import org.springframework.samples.petclinic.owner.VisitStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for querying visit and appointment information from the database. Provides
 * methods to search for visits by date range, pet name, status, and format visit
 * information for display. Supports natural language date parsing for user-friendly query
 * input.
 */
@Service
public class VisitQueryService {

	private final VisitRepository visitRepository;

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a");

	/**
	 * Creates a new visit query service.
	 * @param visitRepository the visit repository for database access
	 */
	public VisitQueryService(VisitRepository visitRepository) {
		this.visitRepository = visitRepository;
	}

	/**
	 * Finds all upcoming visits (from today forward), ordered by date in ascending order.
	 * @return a list of upcoming visits, empty list if none found
	 */
	public List<Visit> findUpcomingVisits() {
		return visitRepository.findByDateGreaterThanEqualOrderByDateAsc(LocalDate.now());
	}

	/**
	 * Finds all visits within the specified date range (inclusive), ordered by date in
	 * ascending order.
	 * @param start the start date of the range
	 * @param end the end date of the range
	 * @return a list of visits within the date range, empty list if none found
	 */
	public List<Visit> findVisitsByDateRange(LocalDate start, LocalDate end) {
		return visitRepository.findByDateBetweenOrderByDateAsc(start, end);
	}

	/**
	 * Finds all upcoming visits for a pet with the given name (case-insensitive). Only
	 * returns visits scheduled for today or later.
	 * @param petName the name of the pet to search for
	 * @return a list of visits for the pet, empty list if none found
	 */
	public List<Visit> findVisitsByPetName(String petName) {
		if (petName == null || petName.isEmpty()) {
			return List.of();
		}

		List<Visit> allUpcomingVisits = findUpcomingVisits();
		return allUpcomingVisits.stream()
			.filter(visit -> visit.getPet() != null && visit.getPet().getName() != null
					&& visit.getPet().getName().equalsIgnoreCase(petName))
			.collect(Collectors.toList());
	}

	/**
	 * Finds all visits with the specified status, ordered by date in ascending order.
	 * @param status the visit status to filter by
	 * @return a list of visits with the specified status, empty list if none found
	 */
	public List<Visit> findVisitsByStatus(VisitStatus status) {
		return visitRepository.findByStatusOrderByDateAsc(status);
	}

	/**
	 * Formats visit information into a human-readable string. Includes pet name, type,
	 * visit type, date, time (if available), and status.
	 * @param visit the visit to format
	 * @return a formatted string with visit information
	 */
	public String formatVisitInfo(Visit visit) {
		StringBuilder info = new StringBuilder();

		// Pet name and type
		if (visit.getPet() != null) {
			info.append(visit.getPet().getName());
			if (visit.getPet().getType() != null) {
				info.append(" (").append(visit.getPet().getType().getName()).append(")");
			}
		}

		// Visit type
		if (visit.getVisitType() != null) {
			info.append(" - ").append(visit.getVisitType().getDisplayName());
		}

		// Date
		if (visit.getDate() != null) {
			info.append(" on ").append(visit.getDate().format(DATE_FORMATTER));
		}

		// Time (if available)
		if (visit.getAppointmentTime() != null) {
			info.append(" at ").append(visit.getAppointmentTime().format(TIME_FORMATTER));
		}

		// Status
		if (visit.getStatus() != null) {
			String statusDisplay = formatStatusDisplay(visit.getStatus());
			info.append(" - Status: ").append(statusDisplay);
		}

		return info.toString();
	}

	/**
	 * Parses natural language date expressions into LocalDate objects. Supports
	 * expressions like "today", "tomorrow", "this week", "next week", "this month", "next
	 * month", and month names like "March".
	 * @param input the natural language date expression
	 * @return the parsed LocalDate, or today's date if parsing fails or input is invalid
	 */
	public LocalDate parseNaturalLanguageDate(String input) {
		if (input == null || input.isEmpty()) {
			return LocalDate.now();
		}

		String normalized = input.toLowerCase().trim();

		// Simple date expressions
		switch (normalized) {
			case "today":
				return LocalDate.now();
			case "tomorrow":
				return LocalDate.now().plusDays(1);
			case "this week":
				return LocalDate.now();
			case "next week":
				return LocalDate.now().plusWeeks(1);
			case "this month":
				return LocalDate.now();
			case "next month":
				return LocalDate.now().plusMonths(1).withDayOfMonth(1);
			default:
				// Try to parse month names
				return parseMonthName(normalized);
		}
	}

	/**
	 * Parses month names into LocalDate objects. Returns the first day of the specified
	 * month, in the current year if the month hasn't passed, otherwise next year.
	 * @param input the month name (case-insensitive)
	 * @return the first day of the specified month, or today's date if parsing fails
	 */
	private LocalDate parseMonthName(String input) {
		String normalized = input.toLowerCase().trim();

		int monthNumber = switch (normalized) {
			case "january", "jan" -> 1;
			case "february", "feb" -> 2;
			case "march", "mar" -> 3;
			case "april", "apr" -> 4;
			case "may" -> 5;
			case "june", "jun" -> 6;
			case "july", "jul" -> 7;
			case "august", "aug" -> 8;
			case "september", "sep", "sept" -> 9;
			case "october", "oct" -> 10;
			case "november", "nov" -> 11;
			case "december", "dec" -> 12;
			default -> 0;
		};

		if (monthNumber == 0) {
			return LocalDate.now(); // Default to today if not recognized
		}

		LocalDate now = LocalDate.now();
		int year = now.getMonthValue() > monthNumber ? now.getYear() + 1 : now.getYear();

		return LocalDate.of(year, monthNumber, 1);
	}

	/**
	 * Formats a VisitStatus enum into a human-readable display string.
	 * @param status the visit status
	 * @return a formatted status string
	 */
	private String formatStatusDisplay(VisitStatus status) {
		return switch (status) {
			case PENDING -> "Pending";
			case SCHEDULED -> "Scheduled";
			case IN_PROGRESS -> "In Progress";
			case COMPLETED -> "Completed";
			case CANCELLED -> "Cancelled";
			case NO_SHOW -> "No Show";
		};
	}

}
