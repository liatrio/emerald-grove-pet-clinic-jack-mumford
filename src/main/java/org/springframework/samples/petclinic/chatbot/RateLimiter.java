package org.springframework.samples.petclinic.chatbot;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Rate limiter to control the number of chatbot requests per session. Uses a token bucket
 * algorithm to limit requests per time period.
 */
@Component
public class RateLimiter {

	private final int maxRequests;

	private final long periodMillis;

	private final ConcurrentHashMap<String, SessionBucket> sessionBuckets;

	/**
	 * Creates a rate limiter with specified limits.
	 * @param maxRequests maximum number of requests allowed per period
	 * @param period the time period duration
	 * @param timeUnit the time unit for the period
	 */
	public RateLimiter(int maxRequests, long period, TimeUnit timeUnit) {
		this.maxRequests = maxRequests;
		this.periodMillis = timeUnit.toMillis(period);
		this.sessionBuckets = new ConcurrentHashMap<>();
	}

	/**
	 * Checks if a request is allowed for the given session.
	 * @param sessionId the session ID
	 * @return true if request is allowed, false otherwise
	 */
	public boolean allowRequest(String sessionId) {
		if (sessionId == null || sessionId.isEmpty()) {
			return false;
		}

		cleanupExpiredSessions();

		SessionBucket bucket = sessionBuckets.computeIfAbsent(sessionId,
				k -> new SessionBucket(maxRequests, periodMillis));

		return bucket.tryConsume();
	}

	/**
	 * Gets the remaining requests for a session.
	 * @param sessionId the session ID
	 * @return the number of remaining requests
	 */
	public int getRemainingRequests(String sessionId) {
		if (sessionId == null || sessionId.isEmpty()) {
			return 0;
		}

		SessionBucket bucket = sessionBuckets.get(sessionId);
		if (bucket == null) {
			return maxRequests;
		}

		return bucket.getRemainingTokens();
	}

	/**
	 * Cleans up expired session buckets to prevent memory leaks.
	 */
	private void cleanupExpiredSessions() {
		long currentTime = System.currentTimeMillis();
		sessionBuckets.entrySet().removeIf(entry -> entry.getValue().isExpired(currentTime));
	}

	/**
	 * Token bucket for a single session.
	 */
	private static class SessionBucket {

		private final int capacity;

		private final long periodMillis;

		private int tokens;

		private long lastRefillTime;

		/**
		 * Creates a new session bucket.
		 * @param capacity maximum number of tokens
		 * @param periodMillis time period in milliseconds for refill
		 */
		SessionBucket(int capacity, long periodMillis) {
			this.capacity = capacity;
			this.periodMillis = periodMillis;
			this.tokens = capacity;
			this.lastRefillTime = System.currentTimeMillis();
		}

		/**
		 * Attempts to consume a token.
		 * @return true if token was consumed, false if no tokens available
		 */
		synchronized boolean tryConsume() {
			refill();
			if (tokens > 0) {
				tokens--;
				return true;
			}
			return false;
		}

		/**
		 * Gets the number of remaining tokens.
		 * @return remaining tokens
		 */
		synchronized int getRemainingTokens() {
			refill();
			return tokens;
		}

		/**
		 * Checks if this bucket has expired.
		 * @param currentTime the current time in milliseconds
		 * @return true if expired
		 */
		synchronized boolean isExpired(long currentTime) {
			return currentTime - lastRefillTime > periodMillis * 2;
		}

		/**
		 * Refills tokens based on elapsed time.
		 */
		private void refill() {
			long currentTime = System.currentTimeMillis();
			long elapsedTime = currentTime - lastRefillTime;

			if (elapsedTime >= periodMillis) {
				tokens = capacity;
				lastRefillTime = currentTime;
			}
		}

	}

}
