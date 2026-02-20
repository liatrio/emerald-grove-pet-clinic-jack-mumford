package org.springframework.samples.petclinic.chatbot;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for chatbot interactions. Handles incoming chat requests, enforces rate
 * limiting, and delegates to the chatbot service for processing.
 */
@RestController
@RequestMapping("/api/chatbot")
@Validated
public class ChatbotController {

	private final ChatbotService chatbotService;

	private final RateLimiter rateLimiter;

	/**
	 * Creates a new chatbot controller.
	 * @param chatbotService the service for processing chat messages
	 * @param rateLimiter the rate limiter for controlling request frequency
	 */
	public ChatbotController(ChatbotService chatbotService, RateLimiter rateLimiter) {
		this.chatbotService = chatbotService;
		this.rateLimiter = rateLimiter;
	}

	/**
	 * Handles incoming chat requests. Validates the request, checks rate limits, and
	 * processes the message using the chatbot service.
	 * @param request the chat request containing the user's message and conversation
	 * history
	 * @param session the HTTP session for rate limiting
	 * @return the chatbot response or error message
	 */
	@PostMapping
	public ResponseEntity<ChatbotResponse> chat(@Valid @RequestBody ChatbotRequest request, HttpSession session) {
		String sessionId = session.getId();

		if (!rateLimiter.allowRequest(sessionId)) {
			return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
				.body(new ChatbotResponse("Rate limit exceeded. Please try again later."));
		}

		try {
			String response = chatbotService.processMessage(request.getMessage(), request.getConversationHistory(),
					"en");
			return ResponseEntity.ok(new ChatbotResponse(response));
		}
		catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ChatbotResponse("An error occurred while processing your request. Please try again."));
		}
	}

}
