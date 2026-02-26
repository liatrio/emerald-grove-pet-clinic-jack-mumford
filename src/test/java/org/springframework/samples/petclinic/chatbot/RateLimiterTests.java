package org.springframework.samples.petclinic.chatbot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for RateLimiter.
 */
class RateLimiterTests {

	private RateLimiter rateLimiter;

	@BeforeEach
	void setUp() {
		// Create rate limiter with 10 requests per minute
		rateLimiter = new RateLimiter(10, 1, TimeUnit.MINUTES);
	}

	@Test
	void testAllowRequestsWithinLimit() {
		// Arrange
		String sessionId = "session-1";

		// Act & Assert - First 10 requests should be allowed
		for (int i = 0; i < 10; i++) {
			assertThat(rateLimiter.allowRequest(sessionId)).as("Request %d should be allowed", i + 1).isTrue();
		}
	}

	@Test
	void testBlockRequestsOverLimit() {
		// Arrange
		String sessionId = "session-1";

		// Act - Use up the limit
		for (int i = 0; i < 10; i++) {
			rateLimiter.allowRequest(sessionId);
		}

		// Assert - 11th request should be blocked
		assertThat(rateLimiter.allowRequest(sessionId)).as("11th request should be blocked").isFalse();
	}

	@Test
	void testDifferentSessionsHaveIndependentLimits() {
		// Arrange
		String session1 = "session-1";
		String session2 = "session-2";

		// Act - Use up limit for session 1
		for (int i = 0; i < 10; i++) {
			rateLimiter.allowRequest(session1);
		}

		// Assert - Session 1 is blocked but session 2 is allowed
		assertThat(rateLimiter.allowRequest(session1)).as("Session 1 should be blocked").isFalse();
		assertThat(rateLimiter.allowRequest(session2)).as("Session 2 should be allowed").isTrue();
	}

	@Test
	void testRateLimitResetsAfterTimePeriod() throws InterruptedException {
		// Arrange
		String sessionId = "session-1";
		// Create limiter with 2 requests per 100ms for faster testing
		RateLimiter fastLimiter = new RateLimiter(2, 100, TimeUnit.MILLISECONDS);

		// Act - Use up the limit
		assertThat(fastLimiter.allowRequest(sessionId)).isTrue();
		assertThat(fastLimiter.allowRequest(sessionId)).isTrue();
		assertThat(fastLimiter.allowRequest(sessionId)).isFalse();

		// Wait for rate limit to reset
		Thread.sleep(150);

		// Assert - Should be allowed again after reset
		assertThat(fastLimiter.allowRequest(sessionId)).as("Request should be allowed after rate limit reset").isTrue();
	}

	@Test
	void testGetRemainingRequests() {
		// Arrange
		String sessionId = "session-1";

		// Act & Assert
		assertThat(rateLimiter.getRemainingRequests(sessionId)).isEqualTo(10);

		rateLimiter.allowRequest(sessionId);
		assertThat(rateLimiter.getRemainingRequests(sessionId)).isEqualTo(9);

		rateLimiter.allowRequest(sessionId);
		rateLimiter.allowRequest(sessionId);
		assertThat(rateLimiter.getRemainingRequests(sessionId)).isEqualTo(7);

		// Use up remaining requests
		for (int i = 0; i < 7; i++) {
			rateLimiter.allowRequest(sessionId);
		}
		assertThat(rateLimiter.getRemainingRequests(sessionId)).isEqualTo(0);
	}

	@Test
	void testCleanupExpiredSessions() throws InterruptedException {
		// Arrange
		String sessionId = "session-1";
		RateLimiter fastLimiter = new RateLimiter(2, 100, TimeUnit.MILLISECONDS);

		// Act - Create session data
		fastLimiter.allowRequest(sessionId);

		// Wait for session to expire
		Thread.sleep(150);

		// Trigger cleanup by making requests with different sessions
		fastLimiter.allowRequest("session-2");
		fastLimiter.allowRequest("session-3");

		// Assert - Old session should have reset
		assertThat(fastLimiter.getRemainingRequests(sessionId))
			.as("Expired session should have full limit after cleanup")
			.isEqualTo(2);
	}

	@Test
	void testNullSessionIdHandling() {
		// Arrange & Act & Assert
		assertThat(rateLimiter.allowRequest(null)).as("Null session should be rejected").isFalse();
		assertThat(rateLimiter.getRemainingRequests(null)).as("Null session should have 0 remaining requests")
			.isEqualTo(0);
	}

	@Test
	void testEmptySessionIdHandling() {
		// Arrange & Act & Assert
		assertThat(rateLimiter.allowRequest("")).as("Empty session should be rejected").isFalse();
		assertThat(rateLimiter.getRemainingRequests("")).as("Empty session should have 0 remaining requests")
			.isEqualTo(0);
	}

}
