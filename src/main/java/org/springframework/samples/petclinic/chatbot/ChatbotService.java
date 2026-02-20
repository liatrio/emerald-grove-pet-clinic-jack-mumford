package org.springframework.samples.petclinic.chatbot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for processing chatbot messages using Claude API. Handles conversation history
 * and API communication with proper error handling.
 */
@Service
public class ChatbotService {

	private final WebClient webClient;

	private final String apiKey;

	private static final Pattern TEXT_PATTERN = Pattern.compile("\"text\"\\s*:\\s*\"([^\"]+)\"");

	private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";

	private static final String CLAUDE_MODEL = "claude-3-5-sonnet-20241022";

	private static final int MAX_TOKENS = 1024;

	private static final int TIMEOUT_SECONDS = 30;

	/**
	 * Creates a new chatbot service.
	 * @param webClient the WebClient for making HTTP requests
	 * @param apiKey the Claude API key
	 */
	public ChatbotService(WebClient webClient, @Value("${claude.api.key}") String apiKey) {
		this.webClient = webClient;
		this.apiKey = apiKey;
	}

	/**
	 * Processes a user message and returns the assistant's response. Includes
	 * conversation history in the API call for context.
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
			Map<String, Object> requestBody = buildRequestBody(message, history);

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
			throw new RuntimeException("Failed to process chatbot message: " + e.getMessage(), e);
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
	 * Extracts the response text from the Claude API JSON response.
	 * @param responseJson the JSON response from Claude API
	 * @return the response text
	 * @throws RuntimeException if response parsing fails
	 */
	private String extractResponseText(String responseJson) {
		try {
			Matcher matcher = TEXT_PATTERN.matcher(responseJson);
			if (matcher.find()) {
				return matcher.group(1);
			}
			throw new RuntimeException("Invalid response format: missing text field");
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to parse API response: " + e.getMessage(), e);
		}
	}

}
