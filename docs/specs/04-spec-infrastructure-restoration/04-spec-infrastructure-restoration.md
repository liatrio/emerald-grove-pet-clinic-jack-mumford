# 04-spec-infrastructure-restoration.md

## Introduction/Overview

All staging AWS infrastructure has been destroyed and must be rebuilt from scratch. This specification covers three related objectives: fixing known bugs in the Terraform configuration, rewriting the three GitHub Actions workflow files to be minimal and clean, and executing the full provisioning sequence to restore the staging environment and verify the end-to-end deployment pipeline works.

## Goals

1. **Fix Terraform bugs** — Correct the ECR repository name, remove duplicate outputs, add `force_delete`, and verify the OIDC backend uses `use_lockfile`
2. **Rewrite workflows as minimal** — Rewrite all three GitHub Actions workflow files to contain only what is necessary, removing excess comments, redundant steps, and verbose scaffolding
3. **Restore OIDC authentication** — Apply the `github-oidc` Terraform module to recreate the IAM role and update the `AWS_ROLE_ARN` GitHub secret
4. **Rebuild staging infrastructure** — Run the init-infrastructure workflow to provision all AWS resources (VPC, RDS, ECR, ECS, ALB) from scratch
5. **Verify end-to-end deployment** — Confirm the deploy workflow succeeds and the application is accessible at the ALB DNS endpoint

## User Stories

**As a DevOps engineer**, I want the Terraform configuration to be bug-free so that `terraform init` and `terraform apply` succeed without errors.

**As a DevOps engineer**, I want the GitHub Actions workflow files to be minimal and readable so that they are easy to maintain and understand at a glance.

**As a DevOps engineer**, I want to restore the complete staging AWS infrastructure from scratch so that the application can be deployed and accessed again.

**As a developer**, I want the deploy workflow to automatically build, push, and deploy my code to ECS so that changes reach the staging environment every time I push to main.

## Demoable Units of Work

### Unit 1: Terraform Bug Fixes

**Purpose:** Correct all four known bugs in the Terraform configuration so that `terraform validate`, `terraform plan`, and `terraform apply` work correctly without errors.

**Functional Requirements:**
- The system shall rename the ECR repository in `terraform/staging/ecr.tf` from `petclinic-staging-ecr-mumford` to `petclinic-staging-repo-mumford` to match the name referenced in all workflow files (Bug 1)
- The system shall remove the `output "ecr_repository_url"` and `output "ecr_repository_arn"` blocks from `terraform/staging/ecr.tf` to eliminate duplicate output errors (Bug 2)
- The system shall add `force_delete = true` to the `aws_ecr_repository` resource in `terraform/staging/ecr.tf` so the repository can be destroyed even when images are present (Bug 3)
- The system shall verify that `terraform/github-oidc/backend.tf` uses `use_lockfile = true` and does not use the deprecated `dynamodb_table` parameter (Bug 4)
- The system shall pass `terraform validate` in both `terraform/staging/` and `terraform/github-oidc/` with no errors after all fixes are applied

**Proof Artifacts:**
- CLI: `terraform validate` output in `terraform/staging/` returning `Success! The configuration is valid.` demonstrates all staging Terraform bugs are resolved
- CLI: `terraform validate` output in `terraform/github-oidc/` returning `Success! The configuration is valid.` demonstrates the OIDC module is valid

---

### Unit 2: Minimal Workflow Rewrites

**Purpose:** Rewrite all three GitHub Actions workflow files to be clean and minimal — preserving all required functional behavior from the original specification while removing unnecessary comments, redundant echo statements, and verbose scaffolding.

**Functional Requirements — `deploy.yml`:**
- The system shall trigger on `push` to `main` and on `pull_request` events (types: opened, synchronize, reopened)
- The system shall declare `permissions: contents: read` and `id-token: write`
- The system shall use concurrency group `deploy-petclinic-staging-service-mumford` with `cancel-in-progress: true` and `timeout-minutes: 30`
- The system shall check out code with `fetch-depth: 0` and `fetch-tags: true`
- The system shall configure AWS credentials via OIDC using `aws-actions/configure-aws-credentials@v6.0.0` with `role-to-assume: ${{ secrets.AWS_ROLE_ARN }}` and `role-session-name: GitHubActions-Deploy`
- The system shall set up JDK 17 (temurin) with Maven cache and run `./mvnw test` before building
- The system shall determine image version from git tag if present, otherwise `sha-${GITHUB_SHA::7}`
- The system shall build and push a Docker image for `linux/amd64` tagged with both the version and `latest` to `277802554323.dkr.ecr.us-east-1.amazonaws.com/petclinic-staging-repo-mumford`
- The system shall save the current task definition ARN for rollback, register a new task definition revision with the updated image (stripping read-only fields), and update the ECS service
- The system shall run `aws ecs wait services-stable` after updating the ECS service
- The system shall retrieve the ALB DNS name and poll `/actuator/health` up to 30 times (10-second intervals) until HTTP 200 and `{"status":"UP"}` is returned
- The system shall set up Node 20 with npm cache, run `npm ci` in `e2e-tests/`, install Playwright chromium, and run `npm test` with `BASE_URL` and `CI=true`
- The system shall upload the E2E test results artifact on every run (including failures)
- The system shall roll back to the saved task definition ARN and wait for ECS stability if any step after checkout fails
- The system shall print a deployment summary (URL and image tag on success, or failure notice) as the final step

**Functional Requirements — `init-infrastructure.yml`:**
- The system shall trigger via `workflow_dispatch` with a single `environment` string input (default: `staging`)
- The system shall validate the environment name is non-empty and matches `^[a-z0-9-]{1,32}$`, failing fast before any AWS calls
- The system shall verify the `terraform/{environment}/` directory exists in the repository
- The system shall configure AWS credentials via OIDC (same pattern as deploy.yml, session name: `GitHubActions-Init`)
- The system shall run `terraform init`, `terraform plan -out=tfplan`, and `terraform apply -auto-approve tfplan` in `terraform/{environment}/`
- The system shall capture Terraform outputs: `alb_dns_name`, `ecr_repository_name`, `ecs_cluster_name`, `ecs_service_name`
- The system shall build and push an initial Docker image tagged `v1.0.0` and `latest` for `linux/amd64`
- The system shall run `sleep 30` then `aws ecs wait services-stable` after pushing the image
- The system shall run the same `/actuator/health` polling loop as the deploy workflow
- The system shall print an infrastructure summary (ALB DNS, ECR repo, ECS cluster/service names) as the final step

**Functional Requirements — `destroy-infrastructure.yml`:**
- The system shall trigger via `workflow_dispatch` with three inputs: `environment` (string), `confirmation_word` (string, must equal `DESTROY` exactly), `acknowledge_deletion` (boolean, must be `true`)
- The system shall validate all three inputs before checkout or any AWS calls, failing immediately if any validation fails
- The system shall configure AWS credentials via OIDC (session name: `GitHubActions-Destroy`)
- The system shall list and batch-delete all ECR images before running Terraform destroy, using `continue-on-error: true`
- The system shall run `terraform init` and `terraform destroy -auto-approve`
- The system shall verify the ECS cluster is deleted after destroy, using `continue-on-error: true`
- The system shall print a destruction summary as the final step

**Proof Artifacts:**
- File: `.github/workflows/deploy.yml` is syntactically valid YAML with `id-token: write` permission, concurrency group, OIDC credentials step, rollback step, and E2E test step demonstrates rewrite is complete and correct
- File: `.github/workflows/init-infrastructure.yml` is syntactically valid YAML with `workflow_dispatch`, environment input validation, and Terraform + Docker steps demonstrates rewrite is complete
- File: `.github/workflows/destroy-infrastructure.yml` is syntactically valid YAML with three-input confirmation, ECR cleanup, and `terraform destroy` demonstrates rewrite is complete

---

### Unit 3: OIDC Bootstrap and Infrastructure Restoration

**Purpose:** Apply the `github-oidc` Terraform module to recreate the IAM role for GitHub Actions, update the `AWS_ROLE_ARN` secret, trigger the init-infrastructure workflow to rebuild all staging resources, and push to main to verify the complete deployment pipeline.

**Functional Requirements:**
- The user shall run `terraform init && terraform apply` in `terraform/github-oidc/` using local AWS SSO credentials to create the OIDC IAM role
- The user shall copy the `github_actions_role_arn` output value and set it as the `AWS_ROLE_ARN` secret in the GitHub repository
- The system shall successfully complete the init-infrastructure workflow (triggered manually from GitHub Actions UI with environment `staging`), provisioning all AWS resources
- The system shall push the initial Docker image (`v1.0.0` and `latest`) to the newly created ECR repository during the init workflow
- The system shall deploy the application to ECS Fargate and pass all health checks during the init workflow
- The system shall successfully complete the deploy workflow (triggered by a push to main), including unit tests, Docker build and push, ECS deployment, health checks, and E2E tests

**Proof Artifacts:**
- CLI: `terraform output github_actions_role_arn` showing the IAM role ARN (e.g., `arn:aws:iam::277802554323:role/github-actions-petclinic-mumford`) demonstrates the OIDC role was created successfully
- GitHub Actions: init-infrastructure workflow run with green status and summary output showing ALB DNS name, ECR repository name, and ECS cluster/service names demonstrates all staging infrastructure was provisioned
- CLI: `curl http://{ALB_DNS}/actuator/health` returning `{"status":"UP"}` demonstrates the application is deployed and healthy on ECS
- GitHub Actions: deploy workflow run (triggered by push to main) with green status demonstrates the full CI/CD pipeline works end-to-end

## Non-Goals (Out of Scope)

1. **New application features**: No changes to the Spring Boot application code, templates, or tests
2. **Infrastructure architecture changes**: No new AWS resources or changes to the Terraform resource structure beyond the four known bug fixes
3. **Additional environments**: Only the staging environment; no production or other environments
4. **Workflow feature additions**: Rewrites are cleanup only — no new functionality (e.g., Slack notifications, image scanning, cost estimation)
5. **Monitoring and alerting**: CloudWatch alarms, dashboards, and log-based alerts are out of scope
6. **DNS or SSL/TLS**: Custom domain names and HTTPS configuration are out of scope

## Design Considerations

No UI/UX design requirements. This is a pure infrastructure and CI/CD automation task with no user-facing interface changes.

## Repository Standards

- **Commit format**: Use `fix:` for Terraform bug fixes, `ci:` for workflow rewrites; include `Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>`
- **PR guidelines**: Keep PRs under 500 lines; any `.md` changes must be in a separate PR per `AGENTS.md`
- **Terraform naming**: Follow `petclinic-{environment}-{resource}-mumford` convention throughout
- **Workflow action versions**: Use pinned versions — `actions/checkout@v4`, `actions/setup-java@v4`, `docker/setup-buildx-action@v3`, `actions/setup-node@v4`, `actions/upload-artifact@v4`, `aws-actions/configure-aws-credentials@v6.0.0`, `hashicorp/setup-terraform@v3`
- **OIDC permissions**: Every workflow that accesses AWS must declare `permissions: id-token: write` and `contents: read`
- **ECR login**: Hardcode region (`us-east-1`) and account ID (`277802554323`) directly in workflow scripts; do not use secrets for these values

## Technical Considerations

- **ECR rename is safe**: Since all infrastructure is already destroyed, renaming from `petclinic-staging-ecr-mumford` to `petclinic-staging-repo-mumford` in Terraform will create a fresh repository with the correct name on the next `terraform apply`
- **Duplicate output removal**: Remove only the `output` blocks from `ecr.tf`; the `aws_ecr_repository` resource itself stays in `ecr.tf`. The `ecr_repository_name` output in `outputs.tf` is unaffected
- **Terraform state backend**: The S3 bucket (`petclinic-terraform-state-mumford`) and DynamoDB table (`petclinic-terraform-locks-mumford`) were not destroyed and remain available for both Terraform modules
- **Apply order**: The `github-oidc` module must be applied locally before triggering any GitHub Actions workflow, because the workflows require the IAM role to authenticate
- **ECS task definition update**: When updating the ECS service in the deploy workflow, strip these read-only fields before registering a new revision: `taskDefinitionArn`, `revision`, `status`, `requiresAttributes`, `compatibilities`, `registeredAt`, `registeredBy`
- **Image versioning**: deploy.yml uses the exact git tag if HEAD is tagged (e.g., `v1.0.0`), otherwise `sha-${GITHUB_SHA::7}`; init workflow always uses `v1.0.0`
- **Health check details**: Poll `/actuator/health` up to 30 times with a 10-second sleep between attempts; expect HTTP 200 and JSON body containing `"status":"UP"`
- **Minimal workflow definition**: "Minimal" means no inline YAML comments, no decorative echo statements, no redundant verification steps; all functional behavior specified above is preserved

## Security Considerations

- **Single required secret**: `AWS_ROLE_ARN` is the only GitHub Secret needed; region and account ID are hardcoded in workflow files, not stored as secrets
- **OIDC trust scope**: The IAM role trust policy restricts token acceptance to `repo:liatrio-forge/emerald-grove-pet-clinic-jack-mumford:*` — no other repositories can assume this role
- **No credentials in proof artifacts**: Screenshots and CLI outputs must not contain AWS access keys, session tokens, or database passwords; AWS account ID (`277802554323`) is acceptable to include
- **Destruction safeguards**: The destroy workflow must enforce three-tier confirmation (environment name, `DESTROY` word, boolean acknowledgment) and validate before any AWS API calls

## Success Metrics

1. **Terraform validation passes**: `terraform validate` returns `Success!` in both `terraform/staging/` and `terraform/github-oidc/` with no errors
2. **Init workflow succeeds**: The init-infrastructure workflow completes with green status, showing the ALB DNS name in its summary output
3. **Application is healthy**: `curl http://{ALB_DNS}/actuator/health` returns HTTP 200 with `{"status":"UP"}`
4. **Deploy workflow passes end-to-end**: A push to main triggers the deploy workflow, which completes all stages (tests, build, deploy, health check, E2E tests) with green status

## Open Questions

No open questions at this time. All requirements have been clarified through the questions round.
