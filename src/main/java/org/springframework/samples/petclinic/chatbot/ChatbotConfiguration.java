package org.springframework.samples.petclinic.chatbot;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.TimeUnit;

/**
 * Configuration for chatbot-related beans.
 */
@Configuration
public class ChatbotConfiguration {

	/**
	 * Creates the WebClient bean for making HTTP requests to Claude API.
	 * @return the configured WebClient
	 */
	@Bean
	public WebClient webClient() {
		return WebClient.builder().build();
	}

	/**
	 * Creates the RateLimiter bean with default configuration.
	 * @return the configured RateLimiter
	 */
	@Bean
	public RateLimiter rateLimiter() {
		return new RateLimiter(10, 1, TimeUnit.MINUTES);
	}

}
