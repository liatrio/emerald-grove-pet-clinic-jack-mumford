package org.springframework.samples.petclinic.chatbot;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a single message in a conversation between user and assistant.
 */
public class ConversationMessage {

	private final String role;

	private final String content;

	/**
	 * Creates a new conversation message.
	 * @param role the role of the message sender ("user" or "assistant")
	 * @param content the content of the message
	 * @throws IllegalArgumentException if role or content is null
	 */
	@JsonCreator
	public ConversationMessage(@JsonProperty("role") String role, @JsonProperty("content") String content) {
		if (role == null) {
			throw new IllegalArgumentException("Role cannot be null");
		}
		if (content == null) {
			throw new IllegalArgumentException("Content cannot be null");
		}
		this.role = role;
		this.content = content;
	}

	/**
	 * Gets the role of the message sender.
	 * @return the role ("user" or "assistant")
	 */
	public String getRole() {
		return role;
	}

	/**
	 * Gets the content of the message.
	 * @return the message content
	 */
	public String getContent() {
		return content;
	}

}
