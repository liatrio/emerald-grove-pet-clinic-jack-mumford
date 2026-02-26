package org.springframework.samples.petclinic.chatbot;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for ChatbotRequest DTO.
 */
class ChatbotRequestTests {

	@Test
	void testChatbotRequestWithMessageAndHistory() {
		// Arrange
		ConversationMessage msg1 = new ConversationMessage("user", "Hello");
		ConversationMessage msg2 = new ConversationMessage("assistant", "Hi there!");
		List<ConversationMessage> history = Arrays.asList(msg1, msg2);

		// Act
		ChatbotRequest request = new ChatbotRequest("What are your hours?", history);

		// Assert
		assertThat(request.getMessage()).isEqualTo("What are your hours?");
		assertThat(request.getConversationHistory()).hasSize(2);
		assertThat(request.getConversationHistory().get(0).getRole()).isEqualTo("user");
		assertThat(request.getConversationHistory().get(0).getContent()).isEqualTo("Hello");
	}

	@Test
	void testChatbotRequestWithEmptyHistory() {
		// Arrange & Act
		ChatbotRequest request = new ChatbotRequest("First message", Collections.emptyList());

		// Assert
		assertThat(request.getMessage()).isEqualTo("First message");
		assertThat(request.getConversationHistory()).isEmpty();
	}

	@Test
	void testChatbotRequestWithNullHistory() {
		// Arrange & Act
		ChatbotRequest request = new ChatbotRequest("Message", null);

		// Assert
		assertThat(request.getMessage()).isEqualTo("Message");
		assertThat(request.getConversationHistory()).isNull();
	}

	@Test
	void testChatbotRequestValidation() {
		// Arrange & Act & Assert
		assertThrows(IllegalArgumentException.class, () -> {
			new ChatbotRequest(null, Collections.emptyList());
		}, "Message cannot be null");
	}

	@Test
	void testChatbotRequestValidationEmptyMessage() {
		// Arrange & Act & Assert
		assertThrows(IllegalArgumentException.class, () -> {
			new ChatbotRequest("", Collections.emptyList());
		}, "Message cannot be empty");
	}

	@Test
	void testChatbotRequestValidationBlankMessage() {
		// Arrange & Act & Assert
		assertThrows(IllegalArgumentException.class, () -> {
			new ChatbotRequest("   ", Collections.emptyList());
		}, "Message cannot be blank");
	}

	// Serialization tests will be validated through integration tests with the controller

	@Test
	void testConversationMessageCreation() {
		// Arrange & Act
		ConversationMessage message = new ConversationMessage("assistant", "Response text");

		// Assert
		assertThat(message.getRole()).isEqualTo("assistant");
		assertThat(message.getContent()).isEqualTo("Response text");
	}

	@Test
	void testConversationMessageValidation() {
		// Arrange & Act & Assert
		assertThrows(IllegalArgumentException.class, () -> {
			new ConversationMessage(null, "Content");
		}, "Role cannot be null");
	}

	@Test
	void testConversationMessageValidationContent() {
		// Arrange & Act & Assert
		assertThrows(IllegalArgumentException.class, () -> {
			new ConversationMessage("user", null);
		}, "Content cannot be null");
	}

}
