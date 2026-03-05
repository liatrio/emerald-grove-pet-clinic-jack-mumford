# 04 Tasks - Infrastructure Restoration

**Spec:** [04-spec-infrastructure-restoration.md](04-spec-infrastructure-restoration.md)

## Relevant Files

- `terraform/staging/ecr.tf` - ECR repository resource; contains all three staging Terraform bugs to fix (name, duplicate outputs, missing force_delete)
- `terraform/github-oidc/backend.tf` - OIDC module backend; verify `use_lockfile = true` is present (Bug 4)
- `terraform/github-oidc/main.tf` - Defines the OIDC IAM role and policies applied in Task 4
- `terraform/github-oidc/outputs.tf` - Outputs `github_actions_role_arn` used to update the GitHub secret
- `.github/workflows/deploy.yml` - Deployment pipeline; rewrite to be minimal while keeping all functional steps
- `.github/workflows/init-infrastructure.yml` - Infrastructure initialization workflow; rewrite to be minimal
- `.github/workflows/destroy-infrastructure.yml` - Infrastructure destruction workflow; rewrite to be minimal
- `docs/specs/04-spec-infrastructure-restoration/04-proofs/` - Directory to create and store proof artifacts for this spec

### Notes

- Tasks 1, 2, and 3 involve only file changes and can be committed and pushed independently.
- Tasks 4 and 5 are operational steps that require AWS SSO credentials and GitHub Actions to be working. They depend on Tasks 1–3 being merged to main first.
- There are no unit tests for Terraform or GitHub Actions workflow files. Validation is via `terraform validate` and YAML syntax checking.
- Follow conventional commit format: `fix:` for Terraform changes, `ci:` for workflow changes. Include `Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>` in all commits.
- Documentation changes (proof artifacts) must be committed in a separate PR from code changes per `AGENTS.md`.

## Tasks

### [x] 1.0 Fix Terraform Bugs in `ecr.tf`

#### 1.0 Proof Artifact(s)

- CLI: `terraform validate` in `terraform/staging/` returns `Success! The configuration is valid.` demonstrates all three staging bugs are resolved
- CLI: `terraform validate` in `terraform/github-oidc/` returns `Success! The configuration is valid.` demonstrates the OIDC module is also clean
- Diff: `git diff terraform/staging/ecr.tf` shows the three targeted changes (name rename, output removal, `force_delete` addition) demonstrates all bugs are fixed

#### 1.0 Tasks

- [x] 1.1 In `terraform/staging/ecr.tf`, change the `name` attribute of the `aws_ecr_repository` resource (line 5) from `"petclinic-staging-ecr-mumford"` to `"petclinic-staging-repo-mumford"`, and update the matching `tags.Name` value on the same resource block (Bug 1)
- [x] 1.2 In `terraform/staging/ecr.tf`, delete the two `output` blocks at the bottom of the file — `output "ecr_repository_url"` and `output "ecr_repository_arn"` (lines 58–67) — these are not present in `outputs.tf` so removing them from `ecr.tf` eliminates the source of the duplicate-output error (Bug 2)
- [x] 1.3 In `terraform/staging/ecr.tf`, add `force_delete = true` as a new attribute inside the `aws_ecr_repository` resource block, directly after the `image_tag_mutability` line (Bug 3)
- [x] 1.4 Open `terraform/github-oidc/backend.tf` and confirm it contains `use_lockfile = true` and does **not** contain `dynamodb_table` — this bug is already fixed, so no change is needed; just confirm (Bug 4)
- [x] 1.5 Run `terraform validate` from inside `terraform/staging/` and confirm the output is `Success! The configuration is valid.`
- [x] 1.6 Run `terraform validate` from inside `terraform/github-oidc/` and confirm the output is `Success! The configuration is valid.`
- [x] 1.7 Commit the changes to `terraform/staging/ecr.tf` with the message: `fix: correct ECR name, remove duplicate outputs, add force_delete`

---

### [x] 2.0 Rewrite `deploy.yml` to Be Minimal

#### 2.0 Proof Artifact(s)

- File: `.github/workflows/deploy.yml` contains no lines starting with `# Sub-task`, no emoji characters, and no echo statements that only print decorative banners — while all functional steps listed below are still present
- CLI: `python3 -c "import yaml, sys; yaml.safe_load(sys.stdin)" < .github/workflows/deploy.yml` exits with code 0 demonstrates the rewritten file is valid YAML

#### 2.0 Tasks

- [x] 2.1 Rewrite `.github/workflows/deploy.yml` from scratch. The new file must include every item in the following checklist and nothing more:
  - `name: Deploy to AWS ECS`
  - `on:` block: `push` to `main`; `pull_request` with `types: [opened, synchronize, reopened]`
  - `concurrency:` group `deploy-petclinic-staging-service-mumford` with `cancel-in-progress: true`
  - `permissions:` `contents: read` and `id-token: write`
  - Single job `deploy` on `ubuntu-latest` with `timeout-minutes: 30`
  - `actions/checkout@v4` with `fetch-depth: 0` and `fetch-tags: true`
  - `aws-actions/configure-aws-credentials@v6.0.0` with `role-to-assume: ${{ secrets.AWS_ROLE_ARN }}`, `aws-region: us-east-1`, `role-session-name: GitHubActions-Deploy`
  - `actions/setup-java@v4` with `distribution: temurin`, `java-version: '17'`, `cache: maven`
  - Run unit tests: `./mvnw test`
  - `docker/setup-buildx-action@v3`
  - ECR login step (`id: login-ecr`) that runs `aws ecr get-login-password` piped to `docker login` and writes `registry=...` to `$GITHUB_OUTPUT`
  - Determine version step (`id: version`) that sets `VERSION` to the exact git tag if one exists on HEAD, otherwise `sha-${GITHUB_SHA::7}`, and writes `version=$VERSION` to `$GITHUB_OUTPUT`
  - Build and push step using `docker buildx build --platform linux/amd64` tagging both `$IMAGE_TAG` and `latest` and pushing to `$ECR_REGISTRY/petclinic-staging-repo-mumford`
  - Update ECS service step (`id: ecs-update`) that: fetches the current task definition ARN and writes it to `$GITHUB_OUTPUT` as `current_task_def`; fetches the full task definition JSON; updates `containerDefinitions[0].image` and strips read-only fields (`taskDefinitionArn`, `revision`, `status`, `requiresAttributes`, `compatibilities`, `registeredAt`, `registeredBy`) using `jq`; registers a new task definition; calls `aws ecs update-service` with the new ARN
  - Wait step: `aws ecs wait services-stable --cluster petclinic-staging-cluster-mumford --services petclinic-staging-service-mumford`
  - Get ALB DNS step (`id: alb`) that calls `aws elbv2 describe-load-balancers --names petclinic-staging-alb-mumford` and writes `alb_dns=...` to `$GITHUB_OUTPUT`
  - Health check step that polls `http://$ALB_DNS/actuator/health` up to 30 times with 10-second sleeps, exits 0 when HTTP 200 and `{"status":"UP"}` is returned, exits 1 if all attempts are exhausted
  - `actions/setup-node@v4` with `node-version: '20'`, `cache: npm`, `cache-dependency-path: e2e-tests/package-lock.json`
  - `npm ci` step with `working-directory: e2e-tests`
  - Playwright install step: `npx playwright install --with-deps chromium` with `working-directory: e2e-tests`
  - E2E test step: `npm test` with `working-directory: e2e-tests`, `CI: true`, `BASE_URL: http://${{ steps.alb.outputs.alb_dns }}`
  - Upload artifact step using `actions/upload-artifact@v4` with `if: always()`, name `e2e-test-results`, paths `e2e-tests/test-results/html-report` and `e2e-tests/test-results/junit.xml`
  - Rollback step with `if: failure()` that calls `aws ecs update-service` with `PREVIOUS_TASK_DEF` and `--force-new-deployment`, then waits for stability
  - Deployment summary step with `if: always()` that prints the application URL and image tag on success, or a failure notice on failure
- [x] 2.2 Verify the rewritten file has **no** lines that begin with `# Sub-task` and **no** emoji characters (e.g., ✅, ❌, 📋)
- [x] 2.3 Run `python3 -c "import yaml, sys; yaml.safe_load(sys.stdin)" < .github/workflows/deploy.yml` and confirm it exits without error
- [x] 2.4 Commit the file with the message: `ci: rewrite deploy workflow to be minimal`

---

### [x] 3.0 Rewrite `init-infrastructure.yml` and `destroy-infrastructure.yml` to Be Minimal

#### 3.0 Proof Artifact(s)

- File: `.github/workflows/init-infrastructure.yml` contains no inline comments, emoji, `Sub-task` annotations, or redundant banner echo statements, while all required functional steps are present
- File: `.github/workflows/destroy-infrastructure.yml` contains no inline comments, emoji, decorative banners, or `sleep 10` pause, while all three input validations run before checkout and all required steps are present
- CLI: `python3 -c "import yaml, sys; yaml.safe_load(sys.stdin)" < .github/workflows/init-infrastructure.yml` and the same for `destroy-infrastructure.yml` both exit with code 0 demonstrates both files are valid YAML

#### 3.0 Tasks

- [x] 3.1 Rewrite `.github/workflows/init-infrastructure.yml` from scratch. The new file must include every item in the following checklist and nothing more:
  - `name: Initialize Infrastructure`
  - `on: workflow_dispatch:` with `environment` input (type: string, required: true, default: staging)
  - `permissions:` `contents: read` and `id-token: write`
  - Single job `init-infrastructure` on `ubuntu-latest` with `timeout-minutes: 45`
  - Input validation step: fails if the environment name is empty or does not match `^[a-z0-9-]{1,32}$`
  - `actions/checkout@v4`
  - Terraform directory verification step: fails if `terraform/${{ github.event.inputs.environment }}/` does not exist
  - `aws-actions/configure-aws-credentials@v6.0.0` with `role-to-assume: ${{ secrets.AWS_ROLE_ARN }}`, `aws-region: us-east-1`, `role-session-name: GitHubActions-Init`
  - `hashicorp/setup-terraform@v3` with `terraform_version: 1.9.0`
  - Terraform init step with `working-directory: terraform/${{ github.event.inputs.environment }}`
  - Terraform plan step with `-out=tfplan`
  - Terraform apply step with `-auto-approve tfplan` (`id: terraform-apply`)
  - Capture Terraform outputs step (`id: tf-outputs`) that writes `alb_dns`, `ecr_repo`, `ecs_cluster`, `ecs_service` to `$GITHUB_OUTPUT` using `terraform output -raw`
  - `actions/setup-java@v4` with `distribution: temurin`, `java-version: '17'`, `cache: maven`
  - Build step: `./mvnw clean package -DskipTests`
  - `docker/setup-buildx-action@v3`
  - ECR login step (`id: login-ecr`) identical in structure to the one in `deploy.yml`
  - Build and push step using `docker buildx build --platform linux/amd64` tagging `v1.0.0` and `latest`, pushing to `$ECR_REGISTRY/petclinic-${{ github.event.inputs.environment }}-repo-mumford`
  - ECS wait step: `sleep 30` then `aws ecs wait services-stable` using the environment-interpolated cluster and service names
  - Health check step identical in logic to the one in `deploy.yml` (30 attempts, 10-second sleep, checks `{"status":"UP"}`)
  - Infrastructure summary step with `if: always()` that prints environment, application URL, ECR registry path, and ECS cluster/service names
- [x] 3.2 Rewrite `.github/workflows/destroy-infrastructure.yml` from scratch. The new file must include every item in the following checklist and nothing more:
  - `name: Destroy Infrastructure`
  - `on: workflow_dispatch:` with three inputs: `environment` (string, required), `confirmation_word` (string, required), `acknowledge_deletion` (boolean, required)
  - `permissions:` `contents: read` and `id-token: write`
  - Single job `destroy-infrastructure` on `ubuntu-latest` with `timeout-minutes: 30`
  - Input validation step **before checkout**: fails if environment name is empty or invalid, fails if `confirmation_word != "DESTROY"`, fails if `acknowledge_deletion != "true"`
  - `actions/checkout@v4`
  - Terraform directory verification step (same logic as init workflow)
  - `aws-actions/configure-aws-credentials@v6.0.0` with `role-session-name: GitHubActions-Destroy`
  - Delete ECR images step with `continue-on-error: true` that: lists all image IDs in `petclinic-${{ github.event.inputs.environment }}-repo-mumford`, runs `aws ecr batch-delete-image` if any images exist
  - `hashicorp/setup-terraform@v3` with `terraform_version: 1.9.0`
  - Terraform init step
  - Terraform destroy step: `terraform destroy -auto-approve`
  - Verify ECS cluster deleted step with `continue-on-error: true` that runs `aws ecs describe-clusters` and prints the result
  - Destruction summary step with `if: always()` that prints the environment name and job status
- [x] 3.3 Verify neither rewritten file contains any lines beginning with `# Sub-task`, any emoji characters, or any decorative banner echo statements (lines of `=` signs)
- [x] 3.4 Run `python3 -c "import yaml, sys; yaml.safe_load(sys.stdin)" < .github/workflows/init-infrastructure.yml` and the same for `destroy-infrastructure.yml`; confirm both exit without error
- [x] 3.5 Commit both files together with the message: `ci: rewrite init and destroy workflows to be minimal`

---

### [ ] 4.0 Bootstrap OIDC Authentication and Initialize Staging Infrastructure

#### 4.0 Proof Artifact(s)

- CLI: `terraform output github_actions_role_arn` in `terraform/github-oidc/` returns an ARN in the format `arn:aws:iam::277802554323:role/github-actions-petclinic-mumford` demonstrates the OIDC role was successfully created
- GitHub Actions: The init-infrastructure workflow run shows green status and the infrastructure summary step output includes a valid ALB DNS name demonstrates all staging AWS resources were provisioned
- CLI: `curl http://{ALB_DNS}/actuator/health` returns `{"status":"UP",...}` with HTTP 200 demonstrates the application is running and healthy on ECS

#### 4.0 Tasks

- [ ] 4.1 Push all commits from Tasks 1–3 to the `main` branch and confirm the GitHub repository is up to date
- [ ] 4.2 Log in to AWS SSO by running `aws sso login` in your terminal and completing the browser authentication flow
- [ ] 4.3 Extract temporary AWS credentials from the SSO cache using the commands in the `<sso>` block of the project context, and export them as environment variables (`AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_SESSION_TOKEN`, `AWS_REGION=us-east-1`)
- [ ] 4.4 Change directory to `terraform/github-oidc/` and run `terraform init` to initialize the backend
- [ ] 4.5 Run `terraform apply -auto-approve` and wait for it to complete; confirm the output includes `github_actions_role_arn = "arn:aws:iam::277802554323:role/github-actions-petclinic-mumford"`
- [ ] 4.6 Copy the `github_actions_role_arn` value from the Terraform output
- [ ] 4.7 In the GitHub repository, go to **Settings → Secrets and variables → Actions** and update the `AWS_ROLE_ARN` secret with the copied role ARN (create it if it doesn't exist)
- [ ] 4.8 In the GitHub repository, go to **Actions → Initialize Infrastructure → Run workflow**, enter `staging` as the environment name, and click **Run workflow**
- [ ] 4.9 Watch the workflow run and wait for it to complete; if it fails, check the logs for the failing step and address the error before retrying
- [ ] 4.10 Once the workflow completes successfully, find the ALB DNS name printed in the infrastructure summary step output
- [ ] 4.11 From your local machine, run `curl http://{ALB_DNS}/actuator/health` (replacing `{ALB_DNS}` with the actual DNS name) and confirm the response body contains `"status":"UP"`
- [ ] 4.12 Record the proof artifacts in `docs/specs/04-spec-infrastructure-restoration/04-proofs/04-task-4.0-proofs.md` (create the `04-proofs/` directory if it doesn't exist): paste the `terraform output` result and `curl` response, and note the GitHub Actions workflow run URL

---

### [ ] 5.0 Verify End-to-End Deployment Pipeline

#### 5.0 Proof Artifact(s)

- GitHub Actions: The deploy workflow run (triggered by a push to main) shows all stages green — unit tests, Docker build+push, ECS update, health check, and E2E tests — demonstrates the full CI/CD pipeline works end-to-end
- GitHub Actions: The E2E test results artifact is present on the completed deploy workflow run and contains passing Playwright test results demonstrates the deployed application is functionally correct

#### 5.0 Tasks

- [ ] 5.1 Make a small, no-op commit on the `main` branch to trigger the deploy workflow — for example, add a blank line to `README.md` or update the commit count in a comment — then push it: `git commit --allow-empty -m "ci: trigger initial deploy verification"` (or make a real change)
- [ ] 5.2 Navigate to **GitHub Actions** and find the running deploy workflow triggered by the push; open the workflow run to monitor its progress in real time
- [ ] 5.3 Wait for all stages to complete: unit tests → Docker build+push → ECS update → ECS wait → health check → E2E tests → artifact upload → deployment summary
- [ ] 5.4 Confirm the workflow run ends with a green status; if any stage fails, check the logs, fix the issue, and re-trigger by pushing another commit
- [ ] 5.5 On the completed workflow run page, open the **Artifacts** section and confirm the `e2e-test-results` artifact was uploaded; download it and verify it contains passing Playwright test output
- [ ] 5.6 Record the proof artifacts in `docs/specs/04-spec-infrastructure-restoration/04-proofs/04-task-5.0-proofs.md`: paste the deployment summary step output (showing the application URL and image tag) and note the E2E test artifact contents
