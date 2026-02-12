# Task 2.0 Proof Artifacts: Implement Global Exception Handler (GREEN Phase)

## Task Summary
Implemented global exception handler with @ControllerAdvice to catch IllegalArgumentException and return user-friendly 404 responses, successfully transitioning from RED to GREEN phase in TDD.

## Proof Artifact 1: Test Output Showing All Tests Pass

### Command Executed
```bash
./mvnw test -Dtest=OwnerControllerTests#testShowOwnerNotFound,OwnerControllerTests#testShowOwnerNotFoundInEdit,PetControllerTests#testShowPetNotFound
```

### Test Results (GREEN Phase)
```
[INFO] Results:
[INFO]
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  9.363 s
[INFO] Finished at: 2026-02-12T08:35:49-08:00
[INFO] ------------------------------------------------------------------------
```

## Proof Artifact 2: Implementation Code

### GlobalExceptionHandler.java (New File)

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ModelAndView handleNotFound(IllegalArgumentException ex) {
        ModelAndView mav = new ModelAndView("notFound");
        mav.setStatus(HttpStatus.NOT_FOUND);

        // Parse exception message to provide context-specific error message
        String exceptionMessage = ex.getMessage();
        String userFriendlyMessage;

        if (exceptionMessage != null && exceptionMessage.contains("Owner")) {
            userFriendlyMessage = "We couldn't find that owner. Please search again or verify the ID.";
        }
        else if (exceptionMessage != null && exceptionMessage.contains("Pet")) {
            userFriendlyMessage = "We couldn't find that pet. Please search again or verify the ID.";
        }
        else {
            userFriendlyMessage = "The requested resource was not found.";
        }

        mav.addObject("errorMessage", userFriendlyMessage);
        mav.addObject("status", 404);

        return mav;
    }
}
```

### PetController.java (Updated)

Added exception handling for missing pets:

```java
@ModelAttribute("pet")
public Pet findPet(@PathVariable("ownerId") int ownerId,
        @PathVariable(name = "petId", required = false) Integer petId) {

    if (petId == null) {
        return new Pet();
    }

    Optional<Owner> optionalOwner = this.owners.findById(ownerId);
    Owner owner = optionalOwner.orElseThrow(() -> new IllegalArgumentException(
            "Owner not found with id: " + ownerId + ". Please ensure the ID is correct "));

    Pet pet = owner.getPet(petId);
    if (pet == null) {
        throw new IllegalArgumentException(
                "Pet not found with id: " + petId + " for owner " + ownerId + ". Please ensure the ID is correct");
    }
    return pet;
}
```

### notFound.html (Minimal Template)

```html
<!DOCTYPE html>
<html xmlns:th="https://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Not Found</title>
</head>
<body>
    <h1>Not Found</h1>
    <p th:text="${errorMessage}">Resource not found</p>
</body>
</html>
```

## Proof Artifact 3: 404 Status and Model Attributes Verification

All tests verify:
- ✅ **HTTP 404 Status**: Exception handler returns `HttpStatus.NOT_FOUND`
- ✅ **View Name "notFound"**: Exception handler returns correct view
- ✅ **Model Attribute "errorMessage"**: Exception handler adds user-friendly message
- ✅ **Model Attribute "status"**: Exception handler adds status code (404)

## Verification

### Test Behavior (GREEN Phase)
✅ All 3 tests PASS - Tests that were failing in RED phase now pass
✅ Exception handler intercepts IllegalArgumentException correctly
✅ Returns 404 HTTP status code instead of 500 server error
✅ Returns user-friendly error messages instead of stack traces
✅ Provides context-specific messages for owner vs pet not found

### Known Issues to Address in Later Tasks
- **I18n Test Failure**: GlobalExceptionHandler contains hardcoded English strings
  - Will be resolved in Task 4.0 (Internationalization Support)
- **Template Enhancement**: notFound.html is minimal
  - Will be enhanced in Task 3.0 (User-Friendly Template with Liatrio Branding)

### Git Commit
```bash
Commit: b4490b0
Message: feat(error-handling): add global exception handler for 404 responses

Changes:
- src/main/java/org/springframework/samples/petclinic/system/GlobalExceptionHandler.java (new)
- src/main/java/org/springframework/samples/petclinic/owner/PetController.java (updated)
- src/main/resources/templates/notFound.html (new, minimal)
- docs/specs/02-spec-friendly-404-pages/02-tasks-friendly-404-pages.md (task tracking)
```

## Next Steps
Proceed to Task 3.0 to enhance the notFound.html template with Liatrio branding, proper styling, and "Find Owners" navigation link.
