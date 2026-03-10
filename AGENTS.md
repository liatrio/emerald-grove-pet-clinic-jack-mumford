# Agent Development Guidelines

This document provides guidelines for AI agents and automated systems working on the Emerald Grove Pet Clinic codebase.

## Code Commit Practices

### Commit Frequently
- Commit code frequently to the working branch
- Make atomic commits that represent single logical changes
- Each commit should have a clear, descriptive message following conventional commit format:
  - `feat:` for new features
  - `fix:` for bug fixes
  - `test:` for test additions/modifications
  - `refactor:` for code refactoring
  - `docs:` for documentation changes
  - `chore:` for maintenance tasks

### Commit Message Format
All commits must include the co-author tag:
```
<type>: <description>

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>
```

## Pull Request Guidelines

### Size Constraints
- Keep pull requests **under 500 lines of code** whenever possible
- Large PRs are harder to review and more likely to introduce bugs
- If a feature requires more than 500 lines, break it into multiple logical PRs
- Only exceed 500 lines when it is **impossible to avoid** (e.g., large data migrations, generated code)

### Readability Requirements
- PRs must be **readable by humans**
- Include clear PR descriptions that explain:
  - What problem is being solved
  - How the solution works
  - Any breaking changes or migration steps
  - Testing approach and coverage
- Structure commits logically so reviewers can follow the progression
- Add comments to complex logic or non-obvious design decisions

### PR Description Template
```markdown
## Summary
Brief description of what this PR accomplishes

## Changes
- Bullet point list of key changes

## Testing
- How was this tested?
- What test coverage was added?

## Related Issues
- Link to related issues or tickets
```

## Documentation Practices

### Separate Documentation PRs
- **Any `.md` file creation or modification MUST be in its own pull request**
- Do NOT merge documentation changes in the same PR as code changes
- This includes:
  - README updates
  - New documentation files
  - Architecture decision records
  - API documentation
  - Deployment guides
  - Developer guides

### Documentation PR Guidelines
- Title format: `docs: <description>`
- Keep documentation PRs focused on a single topic or area
- Ensure documentation is accurate and up-to-date with current code
- Use clear, concise language
- Include examples where applicable

### Exception
The only exception is inline code documentation (JavaDoc, inline comments) which should be included with the code changes they document.

## Code Quality Standards

### Production-Level Requirements
All code produced must meet production-level standards:

1. **Testing**
   - Follow strict Test-Driven Development (TDD)
   - Write tests BEFORE implementing features (Red-Green-Refactor)
   - Maintain >90% code coverage on new code
   - Include unit tests, integration tests, and E2E tests where applicable

2. **Code Style**
   - Follow Google Java Style (2-space indentation)
   - Use meaningful variable and method names
   - Keep methods under 30 lines
   - Apply SOLID principles
   - Use constructor-based dependency injection

3. **Security**
   - No hardcoded credentials or secrets
   - Prevent SQL injection, XSS, and OWASP Top 10 vulnerabilities
   - Validate input at system boundaries
   - Use parameterized queries

4. **Error Handling**
   - Handle errors appropriately
   - Provide meaningful error messages
   - Log errors with appropriate severity levels
   - Don't swallow exceptions silently

5. **Performance**
   - Avoid N+1 query problems
   - Use appropriate caching strategies
   - Consider database indexes for frequent queries
   - Optimize resource usage

6. **Maintainability**
   - Write self-documenting code
   - Add JavaDoc for public methods
   - Avoid premature optimization
   - Don't over-engineer solutions
   - Keep it simple (KISS principle)

## Architecture Compliance

### Follow Existing Patterns
- Use Spring MVC layered architecture (Controller → Repository → Entity)
- Follow repository pattern for data access
- Use Thymeleaf templates with i18n support
- Maintain consistent package structure

### Don't Introduce New Patterns Without Approval
- Don't add new frameworks or major dependencies without discussion
- Don't change architectural patterns without consensus
- Follow the patterns established in CLAUDE.md

## Review Checklist

Before submitting a PR, verify:

- [ ] All tests pass (`./mvnw test`)
- [ ] Code coverage meets requirements (>90%)
- [ ] E2E tests pass (if applicable)
- [ ] Code follows style guidelines
- [ ] No security vulnerabilities introduced
- [ ] PR is under 500 lines (or justification provided)
- [ ] PR description is clear and complete
- [ ] Commit messages follow conventional format
- [ ] Documentation changes are in separate PR
- [ ] All feedback from CI/CD checks addressed

## Continuous Integration

- Monitor GitHub Actions workflow results
- Fix failing tests immediately
- Don't merge PRs with failing checks
- Ensure E2E tests pass before merging

## Pull Request Merging

- **Never merge a pull request yourself**
- Only the repository owner merges pull requests
- Your role is to create PRs and ensure they are ready for review — merging is always a human decision

## Communication

- If blockers are encountered, document them clearly
- If requirements are ambiguous, ask for clarification
- If design decisions need input, propose options with tradeoffs
- Keep stakeholders informed of progress and challenges

---

**Remember**: Quality over speed. Taking time to do it right the first time saves time in the long run.
