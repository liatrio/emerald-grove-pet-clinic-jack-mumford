package org.springframework.samples.petclinic.chatbot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link ChatbotService}.
 */
@ExtendWith(MockitoExtension.class)
class ChatbotServiceTests {

	@Mock
	private WebClient webClient;

	@Mock
	private WebClient.RequestBodyUriSpec requestBodyUriSpec;

	@Mock
	private WebClient.RequestBodySpec requestBodySpec;

	@Mock
	private WebClient.RequestHeadersSpec<?> requestHeadersSpec;

	@Mock
	private WebClient.ResponseSpec responseSpec;

	@Mock
	private PetQueryService petQueryService;

	@Mock
	private VisitQueryService visitQueryService;

	private ChatbotService chatbotService;

	private static final String TEST_API_KEY = "test-api-key";

	@BeforeEach
	@SuppressWarnings("unchecked")
	void setUp() {
		chatbotService = new ChatbotService(webClient, petQueryService, visitQueryService,
				new com.fasterxml.jackson.databind.ObjectMapper(), TEST_API_KEY);

		// Setup basic WebClient mock chain with lenient mode
		lenient().when(webClient.post()).thenReturn(requestBodyUriSpec);
		lenient().when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
		lenient().when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
		lenient().when(requestBodySpec.bodyValue(any())).thenReturn((WebClient.RequestHeadersSpec) requestHeadersSpec);
		lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
	}

	@Test
	void testProcessMessageSuccess() {
		// Arrange
		String userMessage = "What vaccinations does my dog need?";
		List<ConversationMessage> history = new ArrayList<>();
		String locale = "en";

		String mockResponse = """
				{
					"content": [{"text": "Dogs typically need core vaccines like rabies, distemper, parvovirus, and adenovirus."}],
					"role": "assistant"
				}
				""";

		given(responseSpec.bodyToMono(String.class)).willReturn(Mono.just(mockResponse));

		// Act
		String response = chatbotService.processMessage(userMessage, history, locale);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response).contains("Dogs typically need core vaccines");
		verify(webClient).post();
	}

	@Test
	void testProcessMessageWithConversationHistory() {
		// Arrange
		String userMessage = "What about cats?";
		List<ConversationMessage> history = new ArrayList<>();
		history.add(new ConversationMessage("user", "What vaccinations does my dog need?"));
		history.add(new ConversationMessage("assistant", "Dogs need core vaccines like rabies."));
		String locale = "en";

		String mockResponse = """
				{
					"content": [{"text": "Cats need vaccines like rabies, FVRCP, and FeLV."}],
					"role": "assistant"
				}
				""";

		given(responseSpec.bodyToMono(String.class)).willReturn(Mono.just(mockResponse));

		// Act
		String response = chatbotService.processMessage(userMessage, history, locale);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response).contains("Cats need vaccines");
	}

	@Test
	void testProcessMessageWithNullHistory() {
		// Arrange
		String userMessage = "Hello";
		String locale = "en";

		String mockResponse = """
				{
					"content": [{"text": "Hello! How can I help you?"}],
					"role": "assistant"
				}
				""";

		given(responseSpec.bodyToMono(String.class)).willReturn(Mono.just(mockResponse));

		// Act
		String response = chatbotService.processMessage(userMessage, null, locale);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response).contains("Hello");
	}

	@Test
	void testProcessMessageApiFailure() {
		// Arrange
		String userMessage = "Test message";
		List<ConversationMessage> history = new ArrayList<>();
		String locale = "en";

		given(responseSpec.bodyToMono(String.class)).willReturn(Mono.error(new RuntimeException("API error")));

		// Act & Assert
		assertThatThrownBy(() -> chatbotService.processMessage(userMessage, history, locale))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("Failed to process chatbot message");
	}

	@Test
	void testProcessMessageTimeout() {
		// Arrange
		String userMessage = "Test message";
		List<ConversationMessage> history = new ArrayList<>();
		String locale = "en";

		given(responseSpec.bodyToMono(String.class))
			.willReturn(Mono.error(new java.util.concurrent.TimeoutException("Request timeout")));

		// Act & Assert
		assertThatThrownBy(() -> chatbotService.processMessage(userMessage, history, locale))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("Failed to process chatbot message");
	}

	@Test
	void testProcessMessageInvalidResponse() {
		// Arrange
		String userMessage = "Test message";
		List<ConversationMessage> history = new ArrayList<>();
		String locale = "en";

		String invalidResponse = "{ invalid json }";

		given(responseSpec.bodyToMono(String.class)).willReturn(Mono.just(invalidResponse));

		// Act & Assert
		assertThatThrownBy(() -> chatbotService.processMessage(userMessage, history, locale))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("Failed to process chatbot message");
	}

	@Test
	void testProcessMessageEmptyMessage() {
		// Arrange
		String userMessage = "";
		List<ConversationMessage> history = new ArrayList<>();
		String locale = "en";

		// Act & Assert
		assertThatThrownBy(() -> chatbotService.processMessage(userMessage, history, locale))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Message cannot be empty");
	}

	@Test
	void testProcessMessageNullMessage() {
		// Arrange
		List<ConversationMessage> history = new ArrayList<>();
		String locale = "en";

		// Act & Assert
		assertThatThrownBy(() -> chatbotService.processMessage(null, history, locale))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Message cannot be null");
	}

	@Test
	void testProcessMessageWithPetQuery_PetFound() {
		// Arrange
		String userMessage = "What breed is Leo?";
		List<ConversationMessage> history = new ArrayList<>();
		String locale = "en";

		// Mock pet query service to return a pet
		org.springframework.samples.petclinic.owner.Owner owner = new org.springframework.samples.petclinic.owner.Owner();
		owner.setFirstName("George");
		owner.setLastName("Franklin");

		org.springframework.samples.petclinic.owner.Pet pet = new org.springframework.samples.petclinic.owner.Pet();
		pet.setName("Leo");
		org.springframework.samples.petclinic.owner.PetType catType = new org.springframework.samples.petclinic.owner.PetType();
		catType.setName("cat");
		pet.setType(catType);
		pet.setBirthDate(java.time.LocalDate.of(2010, 9, 7));

		PetWithOwner petWithOwner = new PetWithOwner(pet, owner);
		given(petQueryService.findPetByName("Leo")).willReturn(java.util.Optional.of(petWithOwner));
		given(petQueryService.formatPetInfo(pet, owner))
			.willReturn("Leo is a cat, born on 2010-09-07, owned by George Franklin");

		String mockResponse = """
				{
					"content": [{"text": "Leo is a cat breed. He was born on September 7, 2010, and is owned by George Franklin."}],
					"role": "assistant"
				}
				""";

		given(responseSpec.bodyToMono(String.class)).willReturn(Mono.just(mockResponse));

		// Act
		String response = chatbotService.processMessage(userMessage, history, locale);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response).contains("Leo");
		verify(petQueryService).findPetByName("Leo");
	}

	@Test
	void testProcessMessageWithPetQuery_PetNotFound() {
		// Arrange
		String userMessage = "What breed is Fluffy?";
		List<ConversationMessage> history = new ArrayList<>();
		String locale = "en";

		// Mock pet query service to return empty
		given(petQueryService.findPetByName("Fluffy")).willReturn(java.util.Optional.empty());

		String mockResponse = """
				{
					"content": [{"text": "I couldn't find a pet named Fluffy in our records."}],
					"role": "assistant"
				}
				""";

		given(responseSpec.bodyToMono(String.class)).willReturn(Mono.just(mockResponse));

		// Act
		String response = chatbotService.processMessage(userMessage, history, locale);

		// Assert
		assertThat(response).isNotNull();
		verify(petQueryService).findPetByName("Fluffy");
	}

	@Test
	void testProcessMessageWithoutPetQuery() {
		// Arrange
		String userMessage = "What are the clinic hours?";
		List<ConversationMessage> history = new ArrayList<>();
		String locale = "en";

		String mockResponse = """
				{
					"content": [{"text": "Our clinic is open Monday to Friday, 9am to 5pm."}],
					"role": "assistant"
				}
				""";

		given(responseSpec.bodyToMono(String.class)).willReturn(Mono.just(mockResponse));

		// Act
		String response = chatbotService.processMessage(userMessage, history, locale);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response).contains("clinic");
	}

	@Test
	void testProcessMessageWithVisitQuery_UpcomingVisits() {
		// Arrange
		String userMessage = "What appointments are upcoming?";
		List<ConversationMessage> history = new ArrayList<>();
		String locale = "en";

		// Mock visit data
		org.springframework.samples.petclinic.owner.Owner owner = new org.springframework.samples.petclinic.owner.Owner();
		owner.setFirstName("George");
		owner.setLastName("Franklin");

		org.springframework.samples.petclinic.owner.PetType catType = new org.springframework.samples.petclinic.owner.PetType();
		catType.setName("cat");

		org.springframework.samples.petclinic.owner.Pet pet = new org.springframework.samples.petclinic.owner.Pet();
		pet.setName("Leo");
		pet.setType(catType);
		pet.setOwner(owner);

		org.springframework.samples.petclinic.owner.Visit visit = new org.springframework.samples.petclinic.owner.Visit();
		visit.setDate(java.time.LocalDate.now().plusDays(7));
		visit.setAppointmentTime(java.time.LocalDateTime.now().plusDays(7).withHour(10).withMinute(0));
		visit.setVisitType(org.springframework.samples.petclinic.owner.VisitType.CHECKUP);
		visit.setStatus(org.springframework.samples.petclinic.owner.VisitStatus.SCHEDULED);
		visit.setPet(pet);

		given(visitQueryService.findUpcomingVisits()).willReturn(List.of(visit));
		given(visitQueryService.formatVisitInfo(visit))
			.willReturn("Leo (cat) - Wellness Checkup on 2026-03-03 at 10:00 AM - Status: Scheduled");

		String mockResponse = """
				{
					"content": [{"text": "You have one upcoming appointment: Leo (cat) - Wellness Checkup on 2026-03-03 at 10:00 AM."}],
					"role": "assistant"
				}
				""";

		given(responseSpec.bodyToMono(String.class)).willReturn(Mono.just(mockResponse));

		// Act
		String response = chatbotService.processMessage(userMessage, history, locale);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response).contains("appointment");
		verify(visitQueryService).findUpcomingVisits();
	}

	@Test
	void testProcessMessageWithVisitQuery_NoUpcomingVisits() {
		// Arrange
		String userMessage = "What appointments are coming up?";
		List<ConversationMessage> history = new ArrayList<>();
		String locale = "en";

		given(visitQueryService.findUpcomingVisits()).willReturn(List.of());

		String mockResponse = """
				{
					"content": [{"text": "There are no upcoming appointments scheduled."}],
					"role": "assistant"
				}
				""";

		given(responseSpec.bodyToMono(String.class)).willReturn(Mono.just(mockResponse));

		// Act
		String response = chatbotService.processMessage(userMessage, history, locale);

		// Assert
		assertThat(response).isNotNull();
		verify(visitQueryService).findUpcomingVisits();
	}

	@Test
	void testProcessMessageWithInstructionalQuery() {
		// Arrange
		String userMessage = "How do I schedule an appointment?";
		List<ConversationMessage> history = new ArrayList<>();
		String locale = "en";

		String mockResponse = """
				{
					"content": [{"text": "To schedule a visit, navigate to your pet's page and click 'Add Visit'. You can choose the visit type, date, and add any notes."}],
					"role": "assistant"
				}
				""";

		given(responseSpec.bodyToMono(String.class)).willReturn(Mono.just(mockResponse));

		// Act
		String response = chatbotService.processMessage(userMessage, history, locale);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response).contains("schedule");
	}

}
