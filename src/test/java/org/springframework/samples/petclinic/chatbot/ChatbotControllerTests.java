package org.springframework.samples.petclinic.chatbot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for {@link ChatbotController}.
 */
@WebMvcTest(ChatbotController.class)
@Import(ObjectMapper.class)
class ChatbotControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ChatbotService chatbotService;

	@MockitoBean
	private RateLimiter rateLimiter;

	@Autowired
	private ObjectMapper objectMapper;

	private MockHttpSession session;

	@BeforeEach
	void setUp() {
		session = new MockHttpSession();
	}

	@Test
	void testChatWithValidRequest() throws Exception {
		// Arrange
		ChatbotRequest request = new ChatbotRequest("What vaccinations does my dog need?", new ArrayList<>());
		String requestJson = objectMapper.writeValueAsString(request);

		given(rateLimiter.allowRequest(anyString())).willReturn(true);
		given(chatbotService.processMessage(eq(request.getMessage()), any(), anyString()))
			.willReturn("Dogs need core vaccines like rabies, distemper, parvovirus.");

		// Act & Assert
		mockMvc
			.perform(post("/api/chatbot").session(session).contentType(MediaType.APPLICATION_JSON).content(requestJson))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.response").value("Dogs need core vaccines like rabies, distemper, parvovirus."));

		verify(rateLimiter).allowRequest(anyString());
		verify(chatbotService).processMessage(eq(request.getMessage()), any(), anyString());
	}

	@Test
	void testChatWithConversationHistory() throws Exception {
		// Arrange
		List<ConversationMessage> history = new ArrayList<>();
		history.add(new ConversationMessage("user", "What vaccinations does my dog need?"));
		history.add(new ConversationMessage("assistant", "Dogs need core vaccines."));

		ChatbotRequest request = new ChatbotRequest("What about cats?", history);
		String requestJson = objectMapper.writeValueAsString(request);

		given(rateLimiter.allowRequest(anyString())).willReturn(true);
		given(chatbotService.processMessage(anyString(), any(), anyString()))
			.willReturn("Cats need vaccines like rabies, FVRCP.");

		// Act & Assert
		mockMvc
			.perform(post("/api/chatbot").session(session).contentType(MediaType.APPLICATION_JSON).content(requestJson))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.response").value("Cats need vaccines like rabies, FVRCP."));
	}

	@Test
	void testChatWithEmptyMessage() throws Exception {
		// Arrange
		String requestJson = "{\"message\":\"\",\"conversationHistory\":[]}";

		// Act & Assert
		mockMvc
			.perform(post("/api/chatbot").session(session).contentType(MediaType.APPLICATION_JSON).content(requestJson))
			.andExpect(status().isBadRequest());

		verify(chatbotService, never()).processMessage(any(), any(), anyString());
	}

	@Test
	void testChatWithNullMessage() throws Exception {
		// Arrange
		String requestJson = "{\"conversationHistory\":[]}";

		// Act & Assert
		mockMvc
			.perform(post("/api/chatbot").session(session).contentType(MediaType.APPLICATION_JSON).content(requestJson))
			.andExpect(status().isBadRequest());

		verify(chatbotService, never()).processMessage(any(), any(), anyString());
	}

	@Test
	void testChatWithRateLimitExceeded() throws Exception {
		// Arrange
		ChatbotRequest request = new ChatbotRequest("Test message", new ArrayList<>());
		String requestJson = objectMapper.writeValueAsString(request);

		given(rateLimiter.allowRequest(anyString())).willReturn(false);

		// Act & Assert
		mockMvc
			.perform(post("/api/chatbot").session(session).contentType(MediaType.APPLICATION_JSON).content(requestJson))
			.andExpect(status().isTooManyRequests())
			.andExpect(jsonPath("$.response").value("Rate limit exceeded. Please try again later."));

		verify(rateLimiter).allowRequest(anyString());
		verify(chatbotService, never()).processMessage(any(), any(), anyString());
	}

	@Test
	void testChatWithServiceError() throws Exception {
		// Arrange
		ChatbotRequest request = new ChatbotRequest("Test message", new ArrayList<>());
		String requestJson = objectMapper.writeValueAsString(request);

		given(rateLimiter.allowRequest(anyString())).willReturn(true);
		given(chatbotService.processMessage(any(), any(), anyString())).willThrow(new RuntimeException("API error"));

		// Act & Assert
		mockMvc
			.perform(post("/api/chatbot").session(session).contentType(MediaType.APPLICATION_JSON).content(requestJson))
			.andExpect(status().isInternalServerError())
			.andExpect(
					jsonPath("$.response").value("An error occurred while processing your request. Please try again."));

		verify(chatbotService).processMessage(any(), any(), anyString());
	}

	@Test
	void testChatWithInvalidJson() throws Exception {
		// Arrange
		String invalidJson = "{ invalid json }";

		// Act & Assert
		mockMvc
			.perform(post("/api/chatbot").session(session).contentType(MediaType.APPLICATION_JSON).content(invalidJson))
			.andExpect(status().isBadRequest());

		verify(chatbotService, never()).processMessage(any(), any(), anyString());
	}

	@Test
	void testChatWithMessageTooLong() throws Exception {
		// Arrange
		String longMessage = "a".repeat(501);
		ChatbotRequest request = new ChatbotRequest(longMessage, new ArrayList<>());
		String requestJson = objectMapper.writeValueAsString(request);

		// Act & Assert
		mockMvc
			.perform(post("/api/chatbot").session(session).contentType(MediaType.APPLICATION_JSON).content(requestJson))
			.andExpect(status().isBadRequest());

		verify(chatbotService, never()).processMessage(any(), any(), anyString());
	}

}
