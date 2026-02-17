# Emerald Grove Veterinary Clinic

A modern Spring Boot application demonstrating enterprise development best practices with strict Test-Driven Development (TDD).

## Features

- **Owner & Pet Management** - Register owners, manage pets with breed/birth date tracking
- **Visit Scheduling** - Schedule and track veterinary visits with descriptions
- **Veterinarian Directory** - Browse vets with specialties (radiology, surgery, dentistry)
- **Upcoming Visits** - View and filter all future appointments by date, pet type, and owner
- **Multi-Language Support** - 9 languages (English, German, Spanish, Korean, Farsi, Portuguese, Russian, Turkish, Chinese)
- **CSV Export** - Export owner data for reporting and analysis
- **Responsive Design** - Mobile-friendly interface with Liatrio branding

## Tech Stack

- **Backend:** Spring Boot 4.0.0, Spring Data JPA, Hibernate
- **Frontend:** Thymeleaf, Bootstrap 5, Font Awesome
- **Database:** H2 (dev), MySQL, PostgreSQL (production)
- **Testing:** JUnit 5, Mockito, AssertJ, Playwright (E2E)
- **Build:** Maven 3.6+

## Quick Start

### Prerequisites
- Java 17 or later
- Maven 3.6+ (or use included wrapper)

### Run the Application

```bash
./mvnw spring-boot:run
```

Visit: `http://localhost:8080`

### Run with MySQL

```bash
docker run -e MYSQL_USER=petclinic -e MYSQL_PASSWORD=petclinic \
  -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=petclinic \
  -p 3306:3306 mysql:8.4

./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
```

### Run with PostgreSQL

```bash
docker run -e POSTGRES_USER=petclinic -e POSTGRES_PASSWORD=petclinic \
  -e POSTGRES_DB=petclinic -p 5432:5432 postgres:17

./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

### Run Tests

```bash
./mvnw test                    # All tests
./mvnw test -Dtest=*ControllerTests    # Controller tests only
./mvnw test -Dtest=*RepositoryTests    # Repository tests only
```

### Run E2E Tests

```bash
cd e2e-tests
npm ci
npx playwright install
npm test
```

## Development

### Critical Requirement: Strict TDD

**All feature development MUST follow Test-Driven Development:**

1. **RED Phase** - Write a failing test that defines desired behavior
2. **GREEN Phase** - Write minimum code to make the test pass
3. **REFACTOR Phase** - Improve code while maintaining test coverage

**Never write production code before a failing test exists.**

### Project Structure

```
src/main/java/org/springframework/samples/petclinic/
├── owner/           # Owner, Pet, Visit entities & controllers
├── vet/             # Veterinarian & Specialty management
├── model/           # Base entities (BaseEntity, NamedEntity, Person)
└── system/          # Configuration & system utilities

src/main/resources/
├── templates/       # Thymeleaf HTML templates
├── messages/        # i18n message bundles (9 languages)
└── db/              # Database initialization scripts

src/test/java/       # JUnit tests (mirror src structure)
e2e-tests/           # Playwright E2E tests
```

### Development Workflow

1. **Create failing test** (RED)
   ```bash
   # Write test in src/test/java
   ./mvnw test -Dtest=YourTest
   # Verify it fails
   ```

2. **Implement feature** (GREEN)
   ```java
   // Write minimal code to pass test
   ./mvnw test -Dtest=YourTest
   // Verify it passes
   ```

3. **Refactor** (REFACTOR)
   ```bash
   # Improve code quality
   ./mvnw test  # Ensure all tests still pass
   ```

4. **Check coverage**
   ```bash
   ./mvnw test jacoco:report
   open target/site/jacoco/index.html
   # Ensure >90% coverage on new code
   ```

### Code Standards

**Architecture:**
- Spring MVC with layered architecture (Controller → Repository → Entity)
- Constructor-based dependency injection (no field injection)
- Spring Data JPA for data access
- Thymeleaf templates with i18n support

**Testing:**
- `@WebMvcTest` for controller tests (use MockMvc)
- `@DataJpaTest` for repository tests
- Playwright for E2E tests
- Arrange-Act-Assert pattern
- AssertJ for assertions
- >90% line coverage required

**Java:**
- Google Java Style (2-space indentation)
- JavaDoc for public methods
- Methods <30 lines
- SOLID principles

**Commits:**
- Conventional commits: `feat:`, `fix:`, `test:`, `docs:`, `refactor:`
- Atomic commits (one feature per commit)
- Include: `Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>`

### Key Patterns

**Repository Pattern:**
```java
public interface OwnerRepository extends Repository<Owner, Integer> {
    Owner findById(int id);
    Page<Owner> findByLastNameStartingWith(String lastName, Pageable pageable);
    Owner save(Owner owner);
}
```

**Controller Pattern:**
```java
@Controller
@RequestMapping("/owners")
public class OwnerController {
    private final OwnerRepository owners;

    public OwnerController(OwnerRepository owners) {
        this.owners = owners;
    }

    @GetMapping("/{ownerId}")
    public String showOwner(@PathVariable int ownerId, Model model) {
        model.addAttribute("owner", owners.findById(ownerId));
        return "owners/ownerDetails";
    }
}
```

**Test Pattern:**
```java
@WebMvcTest(OwnerController.class)
class OwnerControllerTests {
    @Autowired private MockMvc mockMvc;
    @MockBean private OwnerRepository owners;

    @Test
    void testShowOwner() throws Exception {
        given(owners.findById(1)).willReturn(testOwner());

        mockMvc.perform(get("/owners/1"))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("owner"))
            .andExpect(view().name("owners/ownerDetails"));
    }
}
```

### Database Configuration

**H2 (Default):**
- In-memory database with sample data
- Console: `http://localhost:8080/h2-console`

**MySQL:**
- Profile: `mysql`
- Setup script: `src/main/resources/db/mysql/petclinic_db_setup_mysql.txt`

**PostgreSQL:**
- Profile: `postgres`
- Setup script: `src/main/resources/db/postgres/petclinic_db_setup_postgres.txt`

### Adding Features

1. **Define requirements** - What should the feature do?
2. **Write tests first** - Unit, integration, and E2E tests
3. **Implement with TDD** - Red-Green-Refactor cycle
4. **Verify coverage** - Must exceed 90%
5. **Update i18n** - Add message keys to all 9 language files
6. **Manual testing** - Test in browser across scenarios
7. **Commit** - Conventional commit with co-author tag

### Common Tasks

**Add navigation link:**
```html
<!-- src/main/resources/templates/fragments/layout.html -->
<li th:replace="~{::menuItem ('/path','identifier','title','icon',#{message.key})}">
```

**Add i18n message:**
```properties
# src/main/resources/messages/messages.properties
feature.title=Feature Title
feature.description=Feature description text
```

**Create controller test:**
```java
@WebMvcTest(YourController.class)
class YourControllerTests {
    @Autowired private MockMvc mockMvc;
    @MockBean private YourRepository repository;

    @Test
    void testYourEndpoint() throws Exception {
        // Arrange: Setup mock data
        // Act: Perform request
        // Assert: Verify response
    }
}
```

**Create repository test:**
```java
@DataJpaTest
class YourRepositoryTests {
    @Autowired private YourRepository repository;

    @Test
    void testYourQuery() {
        // Arrange: Setup test data
        // Act: Execute query
        // Assert: Verify results
    }
}
```

**Add E2E test:**
```typescript
// e2e-tests/tests/features/your-feature.spec.ts
test('describes behavior', async ({ page }) => {
    await page.goto('/your/path');
    await expect(page.getByRole('heading')).toBeVisible();
});
```

## IDE Setup

**IntelliJ IDEA:**
- Open project via `File → Open` and select `pom.xml`
- Run configuration created automatically

**VS Code:**
- Install "Extension Pack for Java"
- Use integrated terminal: `./mvnw spring-boot:run`

**Eclipse/STS:**
- Import via `File → Import → Maven → Existing Maven Project`

## Building & Deployment

**Build application:**
```bash
./mvnw clean package
java -jar target/spring-petclinic-*.jar
```

**Build Docker image:**
```bash
./mvnw spring-boot:build-image
docker run -p 8080:8080 spring-petclinic:latest
```

**Production considerations:**
- Use MySQL or PostgreSQL (not H2)
- Configure connection pooling (HikariCP included)
- Enable caching (Spring Cache configured)
- Set appropriate JVM heap size
- Use environment variables for configuration

## Troubleshooting

**Build fails:**
- Verify Java 17+ is active: `java -version`
- Check `JAVA_HOME` environment variable

**Database connection errors:**
- Verify database is running
- Check credentials in `application-{profile}.properties`

**Tests fail:**
- Ensure clean build: `./mvnw clean test`
- Check for port conflicts (8080)

**CSS not updating:**
- Run: `./mvnw package -P css`
- Clear browser cache

## Contributing

1. Fork the repository
2. Create a feature branch
3. **Write tests first** (TDD required)
4. Implement feature with >90% coverage
5. Ensure all tests pass: `./mvnw test`
6. Run E2E tests: `cd e2e-tests && npm test`
7. Verify formatting and style
8. Submit pull request with clear description

## License

Licensed under the Apache License 2.0. See `LICENSE.txt` for details.

## Support

- Report issues: GitHub Issues
- Spring PetClinic Community: [spring-petclinic.github.io](https://spring-petclinic.github.io)
- Spring Boot Documentation: [spring.io/projects/spring-boot](https://spring.io/projects/spring-boot)
