# 03 Questions Round 1 - CI/CD Pipeline

Please answer each question below (select one or more options, or add your own notes). Feel free to add additional context under any question.

## 1. Pipeline Trigger Strategy

Which branches/events should trigger the automated deployment pipeline?

- [ ] (A) Only `main` branch pushes (deploy to staging on every main branch push)
- [ ] (B) `main` and `develop` branches (separate staging/dev environments)
- [ ] (C) All branches with tags (e.g., deploy on git tags like `v1.0.0`)
- [x] (D) Pull requests for preview deployments + main for production
- [ ] (E) Other (describe)

**Additional context:**

## 2. Terraform State Management

Where should Terraform state be stored for the CI/CD pipeline?

- [x] (A) AWS S3 backend with DynamoDB locking (already configured in backend.tf)
- [ ] (B) GitHub repository (committed state files)
- [ ] (C) Terraform Cloud
- [ ] (D) Local state (no remote backend)
- [ ] (E) Other (describe)

**Additional context:**

## 3. AWS Credentials Configuration

How should the GitHub Actions workflows authenticate with AWS?

- [x] (A) GitHub Secrets (AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY, AWS_REGION)
- [ ] (B) GitHub OIDC with AWS IAM Identity Provider (more secure, no long-lived credentials)
- [ ] (C) AWS credentials stored in repository (not recommended)
- [ ] (D) Other (describe)

**Additional context:**

## 4. Docker Build Strategy

How should Docker images be versioned and tagged?

- [ ] (A) Git commit SHA as image tag (e.g., `abc123`) for traceability
- [ ] (B) `latest` tag only (overwrite on each push)
- [x] (C) Semantic versioning (e.g., `v1.2.3`) from git tags
- [ ] (D) Combination: `latest` + commit SHA (both tags)
- [ ] (E) Other (describe)

**Additional context:**

## 5. Deployment Validation

What checks should the pipeline perform before/after deployment?

- [ ] (A) Run unit tests before Docker build
- [ ] (B) Run health check after ECS deployment (verify /actuator/health)
- [ ] (C) Run E2E tests against deployed environment
- [x] (D) All of the above
- [ ] (E) Other (describe)

**Additional context:**

## 6. Rollback Strategy

How should the pipeline handle failed deployments?

- [x] (A) Automatic rollback to previous ECS task definition revision
- [ ] (B) Manual rollback only (pipeline fails, operator decides)
- [ ] (C) Keep both versions running, manually verify, then scale down old
- [ ] (D) No rollback (fix forward only)
- [ ] (E) Other (describe)

**Additional context:**

## 7. Environment Separation

Do you want separate workflows for different environments?

- [x] (A) Single environment (staging only) - current setup
- [ ] (B) Two environments (staging + production) with separate workflows
- [ ] (C) Multiple environments (dev, staging, prod) with promotion workflow
- [ ] (D) Branch-based environments (feature branches deploy to ephemeral environments)
- [ ] (E) Other (describe)

**Additional context:**

## 8. Manual Workflow Triggers

What information should the manual initialization workflow require?

- [x] (A) Environment name only (e.g., "staging" or "production")
- [ ] (B) Terraform variables (e.g., instance sizes, database config)
- [ ] (C) Confirmation checkbox ("I understand this will create AWS resources")
- [ ] (D) All of the above
- [ ] (E) Other (describe)

**Additional context:**

## 9. Destroy Workflow Safety

What safeguards should the manual destroy workflow have?

- [ ] (A) Require environment name input (must match exactly)
- [x] (B) Require confirmation checkbox + typed confirmation word (e.g., "DESTROY")
- [ ] (C) Only allow destroy for non-production environments
- [ ] (D) Create backup/snapshot before destroying
- [ ] (E) Other (describe)

**Additional context:**

## 10. Notifications and Monitoring

How should the pipeline notify on success/failure?

- [x] (A) GitHub workflow status only (visible in Actions tab)
- [ ] (B) Slack notifications on deployment success/failure
- [ ] (C) Email notifications to team
- [ ] (D) GitHub commit status checks on PRs
- [ ] (E) Other (describe)

**Additional context:**

## 11. Terraform Drift Detection

Should the pipeline detect infrastructure drift?

- [ ] (A) Yes, run `terraform plan` on schedule (e.g., daily) to detect drift
- [ ] (B) Yes, but only check drift before deployments
- [x] (C) No, assume Terraform is source of truth
- [ ] (D) Other (describe)

**Additional context:**

## 12. Secrets Management in Pipeline

How should sensitive values be handled in the pipeline?

- [x] (A) All secrets in GitHub Secrets (AWS credentials, database passwords)
- [ ] (B) AWS Secrets Manager for application secrets, GitHub Secrets for AWS auth
- [ ] (C) GitHub Environments with required reviewers for production secrets
- [ ] (D) Other (describe)

**Additional context:**

## 13. Database Migrations

How should database schema changes be handled?

- [x] (A) Run migrations automatically before ECS deployment
- [ ] (B) Separate manual workflow for database migrations
- [ ] (C) No automatic migrations (manual only)
- [ ] (D) Other (describe)

**Additional context:**

## 14. Cost Controls

Should the pipeline include cost estimation or alerts?

- [ ] (A) Run `terraform plan` with cost estimation (e.g., Infracost)
- [ ] (B) Set budget alerts in AWS (separate from pipeline)
- [x] (C) No cost controls in pipeline
- [ ] (D) Other (describe)

**Additional context:**

## 15. Pipeline Approval Gates

Should deployments require manual approval?

- [x] (A) No approval required for staging (auto-deploy)
- [ ] (B) Approval required for production only
- [ ] (C) Approval required for all deployments
- [ ] (D) Approval based on change size (large changes require review)
- [ ] (E) Other (describe)

**Additional context:**
