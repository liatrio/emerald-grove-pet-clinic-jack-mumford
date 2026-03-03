# 03-tasks-cicd-pipeline.md

This task list breaks down the CI/CD Pipeline specification into implementable units of work. Each parent task represents a demoable end-to-end feature with clear proof artifacts.

## Relevant Files

- `.github/workflows/deploy.yml` - Automated deployment pipeline workflow (to be created)
- `.github/workflows/init-infrastructure.yml` - Infrastructure initialization workflow (to be created)
- `.github/workflows/destroy-infrastructure.yml` - Infrastructure destruction workflow (to be created)
- `.github/workflows/e2e-tests.yml` - Existing E2E test workflow (reference for patterns)
- `terraform/staging/*.tf` - Existing Terraform configuration (reference for resource names and outputs)
- `terraform/staging/outputs.tf` - Terraform outputs (may need additional outputs for workflows)
- `Dockerfile` - Existing Docker build configuration (used by workflows)
- `pom.xml` - Maven configuration (used for building application)
- `e2e-tests/` - Existing Playwright E2E tests (executed by deployment workflow)
- `docs/specs/03-spec-cicd-pipeline/03-proofs/` - Directory for proof artifacts (to be created)
- `docs/specs/03-spec-cicd-pipeline/03-proofs/03-task-1.0-proofs.md` - Deployment pipeline proof artifacts (to be created)
- `docs/specs/03-spec-cicd-pipeline/03-proofs/03-task-2.0-proofs.md` - Infrastructure initialization proof artifacts (to be created)
- `docs/specs/03-spec-cicd-pipeline/03-proofs/03-task-3.0-proofs.md` - Infrastructure destruction proof artifacts (to be created)
- `README.md` - Project documentation (to be updated with workflow instructions)

### Notes

- GitHub Actions workflows use YAML syntax and are triggered by push events, pull requests, or manual workflow_dispatch
- All workflows must authenticate to AWS using GitHub Secrets: `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_REGION`
- Docker images must be built with `--platform linux/amd64` for ECS Fargate compatibility
- Use existing resource naming convention: `petclinic-{environment}-{resource-type}-mumford`
- Follow conventional commit format: `ci:`, `docs:`, `feat:` with co-author tag
- Terraform state is managed in S3 backend with DynamoDB locking (already configured in `backend.tf`)
- ECS service name: `petclinic-staging-service-mumford`, Cluster: `petclinic-staging-cluster-mumford`
- ECR repository: `petclinic-staging-repo-mumford`, Region: `us-east-1`
- ALB DNS name available from Terraform output: `alb_dns_name`

## Tasks

### [x] 1.0 Automated Deployment Pipeline

Create a GitHub Actions workflow that automatically builds, tests, and deploys the Pet Clinic application to AWS ECS when code is pushed to main or when pull requests are created. The workflow must include comprehensive testing, health validation, and automatic rollback capabilities to ensure deployment safety.

#### 1.0 Proof Artifact(s)

- Workflow file: `.github/workflows/deploy.yml` exists with complete pipeline configuration demonstrates automation is configured
- Workflow run screenshot: GitHub Actions UI showing successful pipeline execution with all steps passing demonstrates end-to-end deployment works
- ECR images: `aws ecr describe-images --repository-name petclinic-staging-repo-mumford` output showing tagged images demonstrates versioned builds are pushed
- ECS deployment: `aws ecs describe-services --cluster petclinic-staging-cluster-mumford --services petclinic-staging-service-mumford` showing updated task definition demonstrates service was updated
- Health check: `curl http://petclinic-staging-alb-mumford-*.us-east-1.elb.amazonaws.com/actuator/health` returning `{"status":"UP"}` demonstrates application health validation
- E2E test results: Playwright HTML report showing passing tests demonstrates end-to-end functionality validation
- Rollback test: Workflow logs showing automatic rollback after simulated failure demonstrates safety mechanism works

#### 1.0 Tasks

- [x] 1.1 Create base workflow file `.github/workflows/deploy.yml` with workflow name, triggers (push to main, pull_request), and concurrency group to prevent overlapping deployments
- [x] 1.2 Add AWS credentials configuration step using GitHub Secrets for `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, and `AWS_REGION` with `aws-actions/configure-aws-credentials@v4`
- [x] 1.3 Add checkout step with `actions/checkout@v4` to fetch repository code
- [x] 1.4 Add Java 17 setup step with `actions/setup-java@v4` using temurin distribution for Maven builds
- [x] 1.5 Add Maven unit test step running `./mvnw test` to validate code before building Docker image
- [x] 1.6 Add Docker buildx setup with `docker/setup-buildx-action@v3` for multi-platform builds
- [x] 1.7 Add ECR login step using `aws ecr get-login-password` to authenticate Docker with AWS ECR
- [x] 1.8 Add Docker image versioning logic to determine tag from git tag (if present) or use commit SHA as fallback
- [x] 1.9 Add Docker build and push step with `--platform linux/amd64` flag, tagging with version and pushing to ECR repository `petclinic-staging-repo-mumford`
- [x] 1.10 Add ECS service update step using `aws ecs update-service --cluster petclinic-staging-cluster-mumford --service petclinic-staging-service-mumford --force-new-deployment` to trigger deployment
- [x] 1.11 Add deployment wait step using `aws ecs wait services-stable --cluster petclinic-staging-cluster-mumford --services petclinic-staging-service-mumford` to wait for deployment completion
- [x] 1.12 Add ALB DNS retrieval step using `aws elbv2 describe-load-balancers` or Terraform output to get application endpoint
- [x] 1.13 Add health check validation step running `curl` against `/actuator/health` endpoint with retry logic (wait up to 5 minutes for healthy status)
- [x] 1.14 Add E2E test execution step that runs Playwright tests from `e2e-tests/` against the deployed ALB endpoint
- [x] 1.15 Add rollback mechanism that retrieves previous task definition revision and updates ECS service if health checks or E2E tests fail
- [x] 1.16 Add workflow status reporting step to summarize deployment outcome (success, rolled back, or failed)
- [x] 1.17 Test workflow by pushing a commit to main and verifying complete pipeline execution with successful deployment
- [x] 1.18 Test rollback mechanism by temporarily breaking health check endpoint and verifying automatic rollback occurs
- [x] 1.19 Create proof artifact directory `docs/specs/03-spec-cicd-pipeline/03-proofs/` if it doesn't exist
- [x] 1.20 Document proof artifacts in `docs/specs/03-spec-cicd-pipeline/03-proofs/03-task-1.0-proofs.md` with workflow screenshots, CLI outputs, and test results

### [x] 2.0 Infrastructure Initialization Workflow

Create a manual GitHub Actions workflow that initializes complete AWS infrastructure from scratch, including Terraform resource creation, Docker image build, ECR push, and deployment verification. This enables repeatable environment setup for new deployments or disaster recovery scenarios.

#### 2.0 Proof Artifact(s)

- Workflow file: `.github/workflows/init-infrastructure.yml` exists with manual trigger configuration demonstrates initialization workflow is available
- Manual trigger screenshot: GitHub Actions workflow_dispatch UI showing environment input field demonstrates manual execution capability
- Terraform apply output: Workflow logs showing `Apply complete! Resources: X added, 0 changed, 0 destroyed` demonstrates infrastructure creation
- ECR repository: `aws ecr describe-repositories --repository-names petclinic-staging-repo-mumford` output demonstrates ECR was created
- ECS cluster: `aws ecs describe-clusters --clusters petclinic-staging-cluster-mumford` showing ACTIVE status demonstrates cluster initialization
- ALB endpoint: Workflow output showing ALB DNS name demonstrates load balancer provisioning
- Application URL: `curl http://alb-dns/` returning HTTP 200 demonstrates end-to-end infrastructure and application setup

#### 2.0 Tasks

- [x] 2.1 Create workflow file `.github/workflows/init-infrastructure.yml` with workflow_dispatch trigger and environment name input parameter
- [x] 2.2 Add AWS credentials configuration step using GitHub Secrets with `aws-actions/configure-aws-credentials@v4`
- [x] 2.3 Add checkout step with `actions/checkout@v4` to fetch repository code including Terraform configuration
- [x] 2.4 Add Terraform setup step with `hashicorp/setup-terraform@v3` to install Terraform CLI
- [x] 2.5 Add Terraform init step running `terraform init` in `terraform/staging/` directory to initialize backend and download providers
- [x] 2.6 Add Terraform plan step running `terraform plan` to preview infrastructure changes before applying
- [x] 2.7 Add Terraform apply step running `terraform apply -auto-approve` to create all AWS resources (VPC, subnets, security groups, RDS, ECR, ECS, ALB)
- [x] 2.8 Add Terraform output capture step to retrieve ALB DNS name, ECR repository URL, and other key outputs
- [x] 2.9 Add Java 17 setup step for Maven build
- [x] 2.10 Add Maven package step running `./mvnw clean package -DskipTests` to build application JAR
- [x] 2.11 Add Docker buildx setup for multi-platform builds
- [x] 2.12 Add ECR login step using `aws ecr get-login-password`
- [x] 2.13 Add initial Docker image build and push step with `--platform linux/amd64`, tagging as `latest` and `v1.0.0`, pushing to newly created ECR repository
- [x] 2.14 Add ECS service verification step using `aws ecs describe-services` to check if ECS tasks are running and healthy
- [x] 2.15 Add application health check step running `curl` against ALB DNS endpoint `/actuator/health` to verify end-to-end setup
- [x] 2.16 Add workflow summary step outputting ALB DNS name, ECR repository URL, and other key endpoints
- [x] 2.17 Test workflow by triggering it manually from GitHub Actions UI with environment name "staging" (or use non-production environment if available)
- [x] 2.18 Document proof artifacts in `docs/specs/03-spec-cicd-pipeline/03-proofs/03-task-2.0-proofs.md` with workflow screenshots, Terraform outputs, and verification commands

### [ ] 3.0 Infrastructure Destruction Workflow

Create a safe, manual GitHub Actions workflow that completely tears down AWS infrastructure with multiple confirmation safeguards to prevent accidental deletion. The workflow must handle ECR image cleanup before destroying resources and verify complete deletion.

#### 3.0 Proof Artifact(s)

- Workflow file: `.github/workflows/destroy-infrastructure.yml` exists with confirmation inputs demonstrates destruction workflow is available
- Confirmation inputs screenshot: GitHub Actions workflow_dispatch UI showing required confirmations (environment name, "DESTROY" keyword, checkbox) demonstrates safety controls
- Terraform destroy output: Workflow logs showing `Destroy complete! Resources: X destroyed` demonstrates infrastructure removal
- ECR cleanup logs: Workflow output showing deleted image count demonstrates repository cleanup before deletion
- Resource verification: `aws ecs describe-clusters --clusters petclinic-staging-cluster-mumford` returning ResourceNotFoundException demonstrates cluster was deleted
- Terraform state: `terraform show` returning empty state demonstrates all resources removed from state

#### 3.0 Tasks

- [ ] 3.1 Create workflow file `.github/workflows/destroy-infrastructure.yml` with workflow_dispatch trigger and three required inputs: environment name (text), confirmation word (text, must be "DESTROY"), and acknowledgment checkbox
- [ ] 3.2 Add input validation step that checks environment name matches expected value and confirmation word equals "DESTROY" exactly, failing workflow if validations don't pass
- [ ] 3.3 Add warning message step displaying "⚠️ WARNING: This will permanently delete all infrastructure resources" before proceeding
- [ ] 3.4 Add AWS credentials configuration step using GitHub Secrets with `aws-actions/configure-aws-credentials@v4`
- [ ] 3.5 Add checkout step with `actions/checkout@v4` to fetch Terraform configuration
- [ ] 3.6 Add ECR image list step running `aws ecr list-images --repository-name petclinic-staging-repo-mumford` to identify images for deletion
- [ ] 3.7 Add ECR image deletion step running `aws ecr batch-delete-image` to remove all images from repository before destroying ECR itself
- [ ] 3.8 Add Terraform setup step with `hashicorp/setup-terraform@v3`
- [ ] 3.9 Add Terraform init step running `terraform init` in `terraform/staging/` directory
- [ ] 3.10 Add Terraform destroy step running `terraform destroy -auto-approve` to remove all AWS infrastructure resources
- [ ] 3.11 Add resource verification step checking if ECS cluster exists using `aws ecs describe-clusters` (should return ResourceNotFoundException)
- [ ] 3.12 Add resource verification step checking if ECR repository exists using `aws ecr describe-repositories` (should return RepositoryNotFoundException)
- [ ] 3.13 Add Terraform state verification step running `terraform show` to confirm state is empty
- [ ] 3.14 Add workflow summary step reporting number of resources destroyed and confirmation of complete deletion
- [ ] 3.15 Test workflow input validation by attempting to trigger with incorrect confirmation values and verifying it fails early
- [ ] 3.16 Test complete workflow by triggering with correct confirmations in a non-production environment or document the manual testing procedure
- [ ] 3.17 Document proof artifacts in `docs/specs/03-spec-cicd-pipeline/03-proofs/03-task-3.0-proofs.md` with workflow screenshots, destruction outputs, and verification commands
- [ ] 3.18 Update README.md with section explaining how to trigger manual workflows (initialization and destruction) including required inputs and safety warnings
