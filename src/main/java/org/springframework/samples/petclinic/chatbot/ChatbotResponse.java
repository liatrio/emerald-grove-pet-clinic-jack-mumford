package org.springframework.samples.petclinic.chatbot;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Transfer Object for chatbot responses. Contains the assistant's response message.
 */
public class ChatbotResponse {

	private final String response;

	/**
	 * Creates a new chatbot response.
	 * @param response the response message
	 * @throws IllegalArgumentException if response is null or empty
	 */
	@JsonCreator
	public ChatbotResponse(@JsonProperty("response") String response) {
		if (response == null) {
			throw new IllegalArgumentException("Response cannot be null");
		}
		if (response.isEmpty()) {
			throw new IllegalArgumentException("Response cannot be empty");
		}
		this.response = response;
	}

	/**
	 * Gets the response message.
	 * @return the response
	 */
	public String getResponse() {
		return response;
	}

}
