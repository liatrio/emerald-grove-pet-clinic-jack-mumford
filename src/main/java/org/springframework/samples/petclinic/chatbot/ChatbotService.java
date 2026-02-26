package org.springframework.samples.petclinic.chatbot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.Visit;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for processing chatbot messages using Claude API. Handles conversation history
 * and API communication with proper error handling. Integrates with PetQueryService and
 * VisitQueryService to provide database context for pet and visit-related queries.
 */
@Service
public class ChatbotService {

	private static final Logger logger = LoggerFactory.getLogger(ChatbotService.class);

	private final WebClient webClient;

	private final PetQueryService petQueryService;

	private final VisitQueryService visitQueryService;

	private final String apiKey;

	private final ObjectMapper objectMapper;

	private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";

	private static final String CLAUDE_MODEL = "claude-sonnet-4-20250514";

	private static final int MAX_TOKENS = 1024;

	private static final int TIMEOUT_SECONDS = 30;

	// Keywords that suggest the user is asking about pets
	private static final Pattern PET_QUERY_KEYWORDS = Pattern
		.compile("(?i)(pet|breed|born|birthday|age|owner|cat|dog|hamster|bird|snake|lizard)");

	// Keywords that suggest the user is asking about visits/appointments
	private static final Set<String> VISIT_KEYWORDS = Set.of("appointment", "visit", "schedule", "upcoming", "next",
			"checkup", "vaccination", "surgery", "appointments", "visits");

	// Keywords that suggest the user is asking for instructions
	private static final Set<String> INSTRUCTIONAL_KEYWORDS = Set.of("how to", "how do i", "how can i", "help me");

	/**
	 * Creates a new chatbot service.
	 * @param webClient the WebClient for making HTTP requests
	 * @param petQueryService the service for querying pet information
	 * @param visitQueryService the service for querying visit information
	 * @param objectMapper the Jackson ObjectMapper for JSON parsing
	 * @param apiKey the Claude API key
	 */
	public ChatbotService(WebClient webClient, PetQueryService petQueryService, VisitQueryService visitQueryService,
			ObjectMapper objectMapper, @Value("${claude.api.key}") String apiKey) {
		this.webClient = webClient;
		this.petQueryService = petQueryService;
		this.visitQueryService = visitQueryService;
		this.objectMapper = objectMapper;
		this.apiKey = apiKey;
	}

	/**
	 * Processes a user message and returns the assistant's response. Includes
	 * conversation history in the API call for context. If the message appears to be
	 * about pets or visits, queries the database for relevant information and includes it
	 * in the prompt.
	 * @param message the user's message
	 * @param history the conversation history (can be null or empty)
	 * @param locale the user's locale
	 * @return the assistant's response text
	 * @throws IllegalArgumentException if message is null or empty
	 * @throws RuntimeException if API call fails or response is invalid
	 */
	public String processMessage(String message, List<ConversationMessage> history, String locale) {
		if (message == null) {
			throw new IllegalArgumentException("Message cannot be null");
		}
		if (message.isEmpty()) {
			throw new IllegalArgumentException("Message cannot be empty");
		}

		try {
			// Check if message is about pets or visits and add context if needed
			String enhancedMessage = enhanceMessageWithPetContext(message);
			enhancedMessage = enhanceMessageWithVisitContext(enhancedMessage, message);
			enhancedMessage = enhanceMessageWithInstructionalGuidance(enhancedMessage, message);

			Map<String, Object> requestBody = buildRequestBody(enhancedMessage, history);

			String responseJson = webClient.post()
				.uri(CLAUDE_API_URL)
				.header("x-api-key", apiKey)
				.header("anthropic-version", "2023-06-01")
				.header("Content-Type", "application/json")
				.bodyValue(requestBody)
				.retrieve()
				.bodyToMono(String.class)
				.timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
				.block();

			return extractResponseText(responseJson);
		}
		catch (Exception e) {
			logger.error("Failed to process chatbot message", e);
			throw new RuntimeException("Failed to process chatbot message", e);
		}
	}

	/**
	 * Builds the Claude API request body.
	 * @param message the user's message
	 * @param history the conversation history
	 * @return the request body map
	 */
	private Map<String, Object> buildRequestBody(String message, List<ConversationMessage> history) {
		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("model", CLAUDE_MODEL);
		requestBody.put("max_tokens", MAX_TOKENS);

		List<Map<String, String>> messages = new ArrayList<>();

		// Add conversation history
		if (history != null) {
			for (ConversationMessage msg : history) {
				Map<String, String> historyMsg = new HashMap<>();
				historyMsg.put("role", msg.getRole());
				historyMsg.put("content", msg.getContent());
				messages.add(historyMsg);
			}
		}

		// Add current user message
		Map<String, String> userMsg = new HashMap<>();
		userMsg.put("role", "user");
		userMsg.put("content", message);
		messages.add(userMsg);

		requestBody.put("messages", messages);
		return requestBody;
	}

	/**
	 * Extracts the response text from the Claude API JSON response using proper JSON
	 * parsing. This correctly handles escaped characters and quotes within the text.
	 * @param responseJson the JSON response from Claude API
	 * @return the response text
	 * @throws RuntimeException if response parsing fails
	 */
	private String extractResponseText(String responseJson) {
		try {
			JsonNode root = objectMapper.readTree(responseJson);
			JsonNode contentArray = root.get("content");

			if (contentArray != null && contentArray.isArray() && contentArray.size() > 0) {
				JsonNode firstContent = contentArray.get(0);
				JsonNode textNode = firstContent.get("text");

				if (textNode != null) {
					return textNode.asText();
				}
			}

			throw new RuntimeException("Invalid response format: missing content/text field");
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to parse API response: " + e.getMessage(), e);
		}
	}

	/**
	 * Enhances the user message with pet context from the database if the message appears
	 * to be about pets.
	 * @param message the original user message
	 * @return the enhanced message with pet context, or the original message if no pet
	 * context is relevant
	 */
	private String enhanceMessageWithPetContext(String message) {
		// Check if message contains pet-related keywords
		if (!PET_QUERY_KEYWORDS.matcher(message).find()) {
			return message;
		}

		// Try to extract pet names from the message (simple word extraction)
		List<String> potentialPetNames = extractPotentialPetNames(message);

		// Search for matching pets
		StringBuilder petContext = new StringBuilder();
		for (String petName : potentialPetNames) {
			Optional<PetWithOwner> petWithOwner = petQueryService.findPetByName(petName);
			if (petWithOwner.isPresent()) {
				Pet pet = petWithOwner.get().getPet();
				String petInfo = petQueryService.formatPetInfo(pet, petWithOwner.get().getOwner());
				petContext.append("\n[Database Context: ").append(petInfo).append("]");
			}
		}

		// If we found pet context, prepend it to the message
		if (petContext.length() > 0) {
			return petContext + "\n\nUser question: " + message;
		}

		return message;
	}

	/**
	 * Extracts potential pet names from the user message. Looks for capitalized words
	 * that could be pet names.
	 * @param message the user message
	 * @return a list of potential pet names
	 */
	private List<String> extractPotentialPetNames(String message) {
		List<String> names = new ArrayList<>();
		// Match capitalized words (potential names)
		Pattern namePattern = Pattern.compile("\\b([A-Z][a-z]+)\\b");
		Matcher matcher = namePattern.matcher(message);

		while (matcher.find()) {
			String word = matcher.group(1);
			// Filter out common words that aren't names
			if (!isCommonWord(word)) {
				names.add(word);
			}
		}

		return names;
	}

	/**
	 * Checks if a word is a common word that's unlikely to be a pet name.
	 * @param word the word to check
	 * @return true if the word is a common word, false otherwise
	 */
	private boolean isCommonWord(String word) {
		// Common words to exclude (this could be expanded)
		List<String> commonWords = List.of("What", "Where", "When", "Why", "How", "Who", "Which", "Is", "Are", "The",
				"Can", "Could", "Would", "Should", "Do", "Does", "Did", "Has", "Have", "Had");
		return commonWords.contains(word);
	}

	/**
	 * Enhances the user message with visit context from the database if the message
	 * appears to be about visits or appointments.
	 * @param enhancedMessage the message (possibly already enhanced with pet context)
	 * @param originalMessage the original user message
	 * @return the enhanced message with visit context, or the same message if no visit
	 * context is relevant
	 */
	private String enhanceMessageWithVisitContext(String enhancedMessage, String originalMessage) {
		// Check if message contains visit-related keywords
		if (!isVisitRelatedQuery(originalMessage)) {
			return enhancedMessage;
		}

		// Query for upcoming visits
		List<Visit> upcomingVisits = visitQueryService.findUpcomingVisits();

		if (upcomingVisits.isEmpty()) {
			return enhancedMessage + "\n\n[Database Context: There are no upcoming visits scheduled in the system.]";
		}

		// Format visit information
		StringBuilder visitContext = new StringBuilder("\n\n[Database Context: Upcoming visits in the system:\n");
		for (Visit visit : upcomingVisits) {
			String visitInfo = visitQueryService.formatVisitInfo(visit);
			visitContext.append("- ").append(visitInfo).append("\n");
		}
		visitContext.append("]");

		return enhancedMessage + visitContext.toString();
	}

	/**
	 * Enhances the user message with instructional guidance if the message appears to be
	 * asking for instructions on how to use the system.
	 * @param enhancedMessage the message (possibly already enhanced with context)
	 * @param originalMessage the original user message
	 * @return the enhanced message with instructional guidance, or the same message if
	 * not an instructional query
	 */
	private String enhanceMessageWithInstructionalGuidance(String enhancedMessage, String originalMessage) {
		if (!isInstructionalQuery(originalMessage)) {
			return enhancedMessage;
		}

		String guidance = "\n\n[System Guidance: To schedule a visit, navigate to your pet's page and click "
				+ "'Add Visit'. You can choose the visit type (checkup, vaccination, dental, surgery, etc.), "
				+ "select a date and time, and add any notes about the reason for the visit. "
				+ "To view upcoming appointments, go to the 'Visits' section in the main menu.]";

		return enhancedMessage + guidance;
	}

	/**
	 * Checks if the user message appears to be asking about visits or appointments.
	 * @param message the user message
	 * @return true if the message contains visit-related keywords
	 */
	private boolean isVisitRelatedQuery(String message) {
		String lowerMessage = message.toLowerCase();
		return VISIT_KEYWORDS.stream().anyMatch(lowerMessage::contains);
	}

	/**
	 * Checks if the user message appears to be asking for instructions on how to use the
	 * system.
	 * @param message the user message
	 * @return true if the message contains instructional keywords
	 */
	private boolean isInstructionalQuery(String message) {
		String lowerMessage = message.toLowerCase();
		return INSTRUCTIONAL_KEYWORDS.stream().anyMatch(lowerMessage::contains);
	}

}
