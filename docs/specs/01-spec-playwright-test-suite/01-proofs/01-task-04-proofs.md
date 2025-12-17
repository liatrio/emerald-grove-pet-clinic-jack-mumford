# 01 Proofs - Task 4.0 Pet Management Workflow Tests

## CLI - Targeted Pet Tests

```text
> e2e-tests@1.0.0 test
> playwright test --pass-with-no-tests --grep Pet Management

Running 2 tests using 2 workers

  2 passed (2.1s)
```

## Test

- `e2e-tests/tests/features/pet-management.spec.ts`

## Screenshots

- Pet addition form (filled)
  - `docs/specs/01-spec-playwright-test-suite/01-proofs/artifacts/pet-add-form-filled.png`
- Visit scheduling form (filled)
  - `docs/specs/01-spec-playwright-test-suite/01-proofs/artifacts/visit-add-form-filled.png`
- Pet details with visit history
  - `docs/specs/01-spec-playwright-test-suite/01-proofs/artifacts/pet-details-with-visit-history.png`
