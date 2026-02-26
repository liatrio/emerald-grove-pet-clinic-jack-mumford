package org.springframework.samples.petclinic.chatbot;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for ChatbotResponse DTO.
 */
class ChatbotResponseTests {

	@Test
	void testChatbotResponseCreation() {
		// Arrange & Act
		ChatbotResponse response = new ChatbotResponse("This is a response");

		// Assert
		assertThat(response.getResponse()).isEqualTo("This is a response");
	}

	@Test
	void testChatbotResponseValidation() {
		// Arrange & Act & Assert
		assertThrows(IllegalArgumentException.class, () -> {
			new ChatbotResponse(null);
		}, "Response cannot be null");
	}

	@Test
	void testChatbotResponseValidationEmpty() {
		// Arrange & Act & Assert
		assertThrows(IllegalArgumentException.class, () -> {
			new ChatbotResponse("");
		}, "Response cannot be empty");
	}

	// Serialization tests will be validated through integration tests with the controller

	@Test
	void testChatbotResponseWithLongText() {
		// Arrange
		String longText = "This is a very long response that contains multiple sentences. "
				+ "It should be able to handle large amounts of text without any issues. "
				+ "The response can include information about veterinary services, hours, and more.";

		// Act
		ChatbotResponse response = new ChatbotResponse(longText);

		// Assert
		assertThat(response.getResponse()).isEqualTo(longText);
		assertThat(response.getResponse()).hasSizeGreaterThan(100);
	}

}
