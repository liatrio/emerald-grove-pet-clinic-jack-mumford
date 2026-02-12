# Task 1.0 Proof Artifacts: Write Failing JUnit Tests (RED Phase)

## Task Summary
Created failing JUnit tests for 404 handling of missing owners and pets following TDD RED phase.

## Proof Artifact 1: Test Output Showing Failing Tests

### Command Executed
```bash
./mvnw test -Dtest=OwnerControllerTests#testShowOwnerNotFound,OwnerControllerTests#testShowOwnerNotFoundInEdit,PetControllerTests#testShowPetNotFound
```

### Test Results
```
[ERROR] Tests run: 3, Failures: 0, Errors: 3, Skipped: 0

[ERROR] Errors:
[ERROR]   OwnerControllerTests.testShowOwnerNotFound:256 » Servlet Request processing failed:
          java.lang.IllegalArgumentException: Owner not found with id: 999.
          Please ensure the ID is correct and the owner exists in the database.

[ERROR]   OwnerControllerTests.testShowOwnerNotFoundInEdit:267 » Servlet Request processing failed:
          java.lang.IllegalArgumentException: Owner not found with id: 999.
          Please ensure the ID is correct and the owner exists in the database.

[ERROR]   PetControllerTests.testShowPetNotFound:214 » Servlet Request processing failed:
          org.thymeleaf.exceptions.TemplateProcessingException: Exception evaluating SpringEL expression:
          "pet['new']" (template: "pets/createOrUpdatePetForm" - line 8, col 15)

[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
```

## Proof Artifact 2: Test Code Added

### OwnerControllerTests.java - Two New Test Methods

```java
@Test
void testShowOwnerNotFound() throws Exception {
    int nonExistentOwnerId = 999;
    given(this.owners.findById(nonExistentOwnerId)).willReturn(Optional.empty());

    mockMvc.perform(get("/owners/{ownerId}", nonExistentOwnerId))
        .andExpect(status().isNotFound())
        .andExpect(view().name("notFound"))
        .andExpect(model().attributeExists("errorMessage"));
}

@Test
void testShowOwnerNotFoundInEdit() throws Exception {
    int nonExistentOwnerId = 999;
    given(this.owners.findById(nonExistentOwnerId)).willReturn(Optional.empty());

    mockMvc.perform(get("/owners/{ownerId}/edit", nonExistentOwnerId))
        .andExpect(status().isNotFound())
        .andExpect(view().name("notFound"))
        .andExpect(model().attributeExists("errorMessage"));
}
```

### PetControllerTests.java - One New Test Method

```java
@Test
void testShowPetNotFound() throws Exception {
    int nonExistentPetId = 999;
    mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, nonExistentPetId))
        .andExpect(status().isNotFound())
        .andExpect(view().name("notFound"))
        .andExpect(model().attributeExists("errorMessage"));
}
```

## Verification

### Expected Behavior (RED Phase)
✅ All tests FAIL - This is correct for TDD RED phase
✅ Tests expect 404 HTTP status code
✅ Tests expect "notFound" view name
✅ Tests expect "errorMessage" model attribute
✅ Currently failing because no exception handler exists yet

### Failure Analysis
The tests fail with `IllegalArgumentException` being thrown but not caught, which is exactly what we expect before implementing the global exception handler in Task 2.0.

### Git Commit
```bash
Commit: a57ff62
Message: test(error-handling): add failing tests for 404 owner/pet scenarios

Changes:
- src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java (2 new tests)
- src/test/java/org/springframework/samples/petclinic/owner/PetControllerTests.java (1 new test)
- docs/specs/02-spec-friendly-404-pages/02-tasks-friendly-404-pages.md (task tracking)
```

## Next Steps
Proceed to Task 2.0 to implement the global exception handler that will make these tests pass (GREEN phase).
