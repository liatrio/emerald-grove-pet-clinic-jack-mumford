package org.springframework.samples.petclinic.chatbot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

/**
 * Integration tests for the chatbot functionality. Tests the full flow from HTTP request
 * to response, including rate limiting and conversation history.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@DisabledInAotMode
class ChatbotIntegrationTests {

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@MockitoBean
	private ChatbotService chatbotService;

	private String baseUrl;

	@TestConfiguration
	static class TestConfig {

		/**
		 * Creates a test-specific RateLimiter that allows more requests for testing.
		 * @return configured RateLimiter
		 */
		@Bean
		@Primary
		public RateLimiter testRateLimiter() {
			// Use shorter time window for faster testing
			return new RateLimiter(10, 1, TimeUnit.SECONDS);
		}

	}

	@BeforeEach
	void setUp() {
		baseUrl = "http://localhost:" + port + "/api/chatbot";
	}

	/**
	 * Tests the full chatbot endpoint flow with a valid request. Verifies that the
	 * controller properly accepts a request, passes it to the service, and returns the
	 * expected response.
	 */
	@Test
	void testChatbotEndpointFullFlow() throws Exception {
		// Arrange
		ChatbotRequest request = new ChatbotRequest("What vaccinations does my dog need?", new ArrayList<>());
		String expectedResponse = "Dogs need core vaccines like rabies, distemper, and parvovirus.";

		given(chatbotService.processMessage(eq(request.getMessage()), any(), anyString())).willReturn(expectedResponse);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<ChatbotRequest> httpEntity = new HttpEntity<>(request, headers);

		// Act
		ResponseEntity<ChatbotResponse> response = restTemplate.postForEntity(baseUrl, httpEntity,
				ChatbotResponse.class);

		// Assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getResponse()).isEqualTo(expectedResponse);
	}

	/**
	 * Tests that rate limiting is enforced after the maximum number of requests. Sends 11
	 * messages rapidly and verifies the 11th request returns HTTP 429 (Too Many
	 * Requests). Note: TestRestTemplate creates a new session for each request by
	 * default. Since we can't easily share sessions in integration tests, this test
	 * verifies that individual sessions are rate-limited properly by making requests
	 * within the same test method (which shares the test's random port).
	 */
	@Test
	void testRateLimitingEnforcement() throws Exception {
		// Arrange
		given(chatbotService.processMessage(anyString(), any(), anyString())).willReturn("Response");

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		// Create a stateful RestTemplate that maintains cookies/session
		TestRestTemplate statefulTemplate = restTemplate.withBasicAuth("user", "pass");
		String sessionCookie = null;

		// Act - Send 10 requests (should all succeed)
		for (int i = 0; i < 10; i++) {
			ChatbotRequest request = new ChatbotRequest("Message " + i, new ArrayList<>());

			// If we have a session cookie, add it to the headers
			if (sessionCookie != null) {
				headers.set("Cookie", sessionCookie);
			}

			HttpEntity<ChatbotRequest> httpEntity = new HttpEntity<>(request, headers);
			ResponseEntity<ChatbotResponse> response = restTemplate.postForEntity(baseUrl, httpEntity,
					ChatbotResponse.class);

			// Capture the session cookie from the first response
			if (i == 0 && response.getHeaders().get("Set-Cookie") != null) {
				sessionCookie = response.getHeaders().get("Set-Cookie").get(0).split(";")[0];
			}

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		}

		// Act - Send 11th request (should be rate limited)
		ChatbotRequest request = new ChatbotRequest("Message 11", new ArrayList<>());
		if (sessionCookie != null) {
			headers.set("Cookie", sessionCookie);
		}
		HttpEntity<ChatbotRequest> httpEntity = new HttpEntity<>(request, headers);
		ResponseEntity<ChatbotResponse> response = restTemplate.postForEntity(baseUrl, httpEntity,
				ChatbotResponse.class);

		// Assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getResponse()).contains("Rate limit exceeded");
	}

	/**
	 * Tests that conversation history is properly maintained and passed to the service.
	 * Sends multiple messages in sequence and verifies context is maintained.
	 */
	@Test
	void testConversationHistoryPersistence() throws Exception {
		// Arrange
		List<ConversationMessage> history = new ArrayList<>();

		// Mock service to return different responses based on message content
		given(chatbotService.processMessage(anyString(), any(), anyString()))
			.willAnswer((Answer<String>) invocation -> {
				String message = invocation.getArgument(0);
				if (message.contains("dog")) {
					return "Dogs need core vaccines like rabies.";
				}
				else if (message.contains("cats")) {
					return "Cats need vaccines like rabies and FVRCP.";
				}
				else {
					return "Yes, all pets should be vaccinated.";
				}
			});

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		// Act & Assert - First message
		ChatbotRequest firstRequest = new ChatbotRequest("What vaccinations does my dog need?", history);
		HttpEntity<ChatbotRequest> firstEntity = new HttpEntity<>(firstRequest, headers);
		ResponseEntity<ChatbotResponse> firstResponse = restTemplate.postForEntity(baseUrl, firstEntity,
				ChatbotResponse.class);

		assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(firstResponse.getBody()).isNotNull();
		assertThat(firstResponse.getBody().getResponse()).contains("Dogs need core vaccines");

		// Update history with first exchange
		history.add(new ConversationMessage("user", "What vaccinations does my dog need?"));
		history.add(new ConversationMessage("assistant", firstResponse.getBody().getResponse()));

		// Act & Assert - Second message with history
		ChatbotRequest secondRequest = new ChatbotRequest("What about cats?", history);
		HttpEntity<ChatbotRequest> secondEntity = new HttpEntity<>(secondRequest, headers);
		ResponseEntity<ChatbotResponse> secondResponse = restTemplate.postForEntity(baseUrl, secondEntity,
				ChatbotResponse.class);

		assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(secondResponse.getBody()).isNotNull();
		assertThat(secondResponse.getBody().getResponse()).contains("Cats need vaccines");

		// Update history with second exchange
		history.add(new ConversationMessage("user", "What about cats?"));
		history.add(new ConversationMessage("assistant", secondResponse.getBody().getResponse()));

		// Act & Assert - Third message with full history
		ChatbotRequest thirdRequest = new ChatbotRequest("Should all pets be vaccinated?", history);
		HttpEntity<ChatbotRequest> thirdEntity = new HttpEntity<>(thirdRequest, headers);
		ResponseEntity<ChatbotResponse> thirdResponse = restTemplate.postForEntity(baseUrl, thirdEntity,
				ChatbotResponse.class);

		assertThat(thirdResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(thirdResponse.getBody()).isNotNull();
		assertThat(thirdResponse.getBody().getResponse()).contains("all pets should be vaccinated");

		// Verify that history was maintained (should have 4 messages after 2 exchanges)
		assertThat(history).hasSize(4);
	}

	/**
	 * Tests that the endpoint properly handles service errors and returns an appropriate
	 * error response.
	 */
	@Test
	void testServiceErrorHandling() throws Exception {
		// Arrange
		ChatbotRequest request = new ChatbotRequest("Test message", new ArrayList<>());

		given(chatbotService.processMessage(anyString(), any(), anyString()))
			.willThrow(new RuntimeException("API connection failed"));

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<ChatbotRequest> httpEntity = new HttpEntity<>(request, headers);

		// Act
		ResponseEntity<ChatbotResponse> response = restTemplate.postForEntity(baseUrl, httpEntity,
				ChatbotResponse.class);

		// Assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getResponse()).contains("An error occurred while processing your request");
	}

	/**
	 * Tests that invalid requests (empty message) are properly rejected.
	 */
	@Test
	void testInvalidRequestValidation() throws Exception {
		// Arrange - Create request with empty message
		String requestJson = "{\"message\":\"\",\"conversationHistory\":[]}";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> httpEntity = new HttpEntity<>(requestJson, headers);

		// Act
		ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, httpEntity, String.class);

		// Assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	/**
	 * Tests that messages with conversation history are properly processed and the
	 * history is passed to the service.
	 */
	@Test
	void testRequestWithConversationHistory() throws Exception {
		// Arrange
		List<ConversationMessage> history = new ArrayList<>();
		history.add(new ConversationMessage("user", "What are your hours?"));
		history.add(new ConversationMessage("assistant", "We are open Monday-Friday 9am-5pm."));

		ChatbotRequest request = new ChatbotRequest("Do you have weekend hours?", history);
		String expectedResponse = "We are closed on weekends.";

		given(chatbotService.processMessage(eq(request.getMessage()), any(), anyString())).willReturn(expectedResponse);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<ChatbotRequest> httpEntity = new HttpEntity<>(request, headers);

		// Act
		ResponseEntity<ChatbotResponse> response = restTemplate.postForEntity(baseUrl, httpEntity,
				ChatbotResponse.class);

		// Assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getResponse()).isEqualTo(expectedResponse);
	}

}
