/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.sql.Date;
import java.time.LocalDate;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(properties = { "spring.docker.compose.enabled=false", "user.timezone=America/Los_Angeles" })
@ActiveProfiles("postgres")
@Testcontainers(disabledWithoutDocker = true)
@DisabledInNativeImage
class PostgresSequenceResetIntegrationTests {

	@ServiceConnection
	@Container
	static PostgreSQLContainer<?> container = new PostgreSQLContainer<>(DockerImageName.parse("postgres:18.1"))
		.withEnv("TZ", "America/Los_Angeles");

	@Autowired
	private DataSource dataSource;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@BeforeAll
	static void available() {
		assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker not available");
		// Set timezone to a format PostgreSQL recognizes
		System.setProperty("user.timezone", "America/Los_Angeles");
		java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("America/Los_Angeles"));
	}

	@Test
	void shouldAllowInsertsAfterDataSqlResetsSequences() {
		// Arrange
		resetSequence("owners");
		resetSequence("pets");
		resetSequence("visits");

		ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
				new ClassPathResource("db/postgres/data.sql"));
		populator.execute(dataSource);

		// Act + Assert
		assertThatCode(() -> jdbcTemplate.update(
				"INSERT INTO owners (first_name, last_name, address, city, telephone) VALUES (?, ?, ?, ?, ?)", "Casey",
				"Harper", "42 Juniper Way", "Madison", "6085554444"))
			.doesNotThrowAnyException();

		assertThatCode(
				() -> jdbcTemplate.update("INSERT INTO pets (name, birth_date, type_id, owner_id) VALUES (?, ?, ?, ?)",
						"Comet", Date.valueOf(LocalDate.of(2018, 5, 20)), 2, 1))
			.doesNotThrowAnyException();

		assertThatCode(
				() -> jdbcTemplate.update("INSERT INTO visits (pet_id, visit_date, description) VALUES (?, ?, ?)", 1,
						Date.valueOf(LocalDate.of(2026, 1, 26)), "Routine checkup"))
			.doesNotThrowAnyException();
	}

	private void resetSequence(String tableName) {
		jdbcTemplate.execute("SELECT setval(pg_get_serial_sequence('" + tableName + "', 'id'), 1, true)");
	}

}
