package org.springframework.samples.petclinic.chatbot;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Data Transfer Object for chatbot requests. Contains the user's message and conversation
 * history.
 */
public class ChatbotRequest {

	@NotBlank(message = "Message cannot be empty")
	@Size(max = 500, message = "Message cannot exceed 500 characters")
	private final String message;

	private final List<ConversationMessage> conversationHistory;

	/**
	 * Creates a new chatbot request.
	 * @param message the user's message
	 * @param conversationHistory the conversation history (can be null or empty)
	 */
	@JsonCreator
	public ChatbotRequest(@JsonProperty("message") String message,
			@JsonProperty("conversationHistory") List<ConversationMessage> conversationHistory) {
		this.message = message;
		this.conversationHistory = conversationHistory;
	}

	/**
	 * Gets the user's message.
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Gets the conversation history.
	 * @return the conversation history (may be null or empty)
	 */
	public List<ConversationMessage> getConversationHistory() {
		return conversationHistory;
	}

}
