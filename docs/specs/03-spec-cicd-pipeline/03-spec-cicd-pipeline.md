# 03-spec-cicd-pipeline.md

## Introduction/Overview

This specification defines a comprehensive CI/CD pipeline using GitHub Actions to automate the deployment of the Spring Boot Pet Clinic application to AWS ECS Fargate. The pipeline will handle automated builds and deployments on pull requests and main branch merges, with additional manual workflows for infrastructure initialization and destruction. The goal is to enable rapid, reliable deployments while maintaining infrastructure as code principles and providing safe mechanisms for environment management.

## Goals

1. **Automate application deployment** - Trigger automated Docker builds and ECS deployments on code changes to main branch
2. **Enable preview deployments** - Deploy to staging environment automatically on pull requests for testing before merge
3. **Provide infrastructure lifecycle management** - Create manual workflows for first-time setup and complete teardown of AWS resources
4. **Ensure deployment safety** - Implement automated testing, health checks, and automatic rollback on failures
5. **Maintain deployment traceability** - Use semantic versioning for Docker images and track deployments through GitHub Actions

## User Stories

**As a developer**, I want to automatically deploy my code changes to staging when I push to the main branch, so that I can see my changes in a live environment without manual intervention.

**As a developer**, I want to preview my changes in the staging environment when I create a pull request, so that I can validate functionality before merging to main.

**As a DevOps engineer**, I want to initialize the complete AWS infrastructure with a single workflow run, so that I can set up new environments quickly and consistently.

**As a DevOps engineer**, I want to safely destroy infrastructure when needed, so that I can manage costs and clean up test environments without manually deleting resources.

**As a team lead**, I want deployments to automatically roll back on failure, so that the application remains available even if a bad deployment is pushed.

## Demoable Units of Work

### Unit 1: Automated Deployment Pipeline

**Purpose:** Automatically build, test, and deploy the application to AWS ECS when code is pushed to main or when pull requests are created, ensuring continuous delivery of tested changes.

**Functional Requirements:**
- The system shall trigger the deployment pipeline on push to main branch and on pull request events
- The system shall run Maven unit tests before building the Docker image to catch errors early
- The system shall build a Docker image with AMD64 platform compatibility for ECS Fargate
- The system shall tag Docker images with semantic version tags from git tags (e.g., v1.0.0)
- The system shall authenticate to AWS ECR using GitHub Secrets and push the built image
- The system shall update the ECS service to deploy the new Docker image using force new deployment
- The system shall wait for ECS deployment to complete and verify service health
- The system shall execute health checks against the /actuator/health endpoint after deployment
- The system shall run E2E tests against the deployed staging environment to validate functionality
- The system shall automatically roll back to the previous ECS task definition revision if health checks or E2E tests fail
- The system shall report deployment status via GitHub Actions workflow status

**Proof Artifacts:**
- GitHub Actions workflow file: `.github/workflows/deploy.yml` demonstrates pipeline configuration exists
- Workflow run screenshot: GitHub Actions UI showing successful pipeline execution demonstrates automation works
- ECR image list: `aws ecr describe-images` output showing versioned images demonstrates image versioning
- ECS service status: `aws ecs describe-services` showing updated task definition demonstrates deployment success
- Health check output: `curl http://alb-dns/actuator/health` returning UP status demonstrates application health validation
- E2E test results: Playwright test report showing passing tests demonstrates end-to-end functionality validation

### Unit 2: Infrastructure Initialization Workflow

**Purpose:** Provide a manual GitHub Actions workflow to initialize complete AWS infrastructure from scratch, enabling repeatable environment setup for new deployments or disaster recovery.

**Functional Requirements:**
- The system shall provide a manually triggered workflow with workflow_dispatch event
- The user shall provide an environment name input (e.g., "staging") to target specific configurations
- The system shall initialize Terraform backend connection to S3 with DynamoDB locking
- The system shall run `terraform init` to download required providers and modules
- The system shall execute `terraform plan` to preview infrastructure changes before applying
- The system shall run `terraform apply -auto-approve` to create all AWS resources (VPC, subnets, security groups, RDS, ECR, ECS, ALB)
- The system shall build an initial Docker image with AMD64 platform after infrastructure is ready
- The system shall push the initial Docker image to the newly created ECR repository
- The system shall run database migrations against the RDS instance after infrastructure is up
- The system shall verify the ECS service is running and healthy before completing
- The system shall output the ALB DNS name and other key infrastructure endpoints

**Proof Artifacts:**
- GitHub Actions workflow file: `.github/workflows/init-infrastructure.yml` demonstrates initialization workflow exists
- Workflow run screenshot: Manual workflow trigger UI showing environment input demonstrates manual execution
- Terraform output: `terraform apply` success message showing resource count demonstrates infrastructure creation
- ECR verification: `aws ecr describe-repositories` output demonstrates ECR repository created
- ECS cluster status: `aws ecs describe-clusters` showing ACTIVE cluster demonstrates cluster initialization
- ALB DNS output: Workflow logs showing ALB endpoint demonstrates load balancer provisioning
- Application accessibility: `curl http://alb-dns/` returning HTTP 200 demonstrates end-to-end setup success

### Unit 3: Infrastructure Destruction Workflow

**Purpose:** Provide a safe, manually triggered workflow to tear down complete AWS infrastructure, including confirmation safeguards to prevent accidental deletion.

**Functional Requirements:**
- The system shall provide a manually triggered workflow with workflow_dispatch event and required confirmations
- The user shall provide an environment name input that must match the target environment exactly
- The user shall type a confirmation word "DESTROY" to proceed with infrastructure deletion
- The system shall require a confirmation checkbox stating "I understand this will permanently delete all resources"
- The system shall verify all confirmations before proceeding with any destructive actions
- The system shall delete all Docker images from ECR repository before destroying the repository
- The system shall run `terraform destroy -auto-approve` to remove all AWS infrastructure
- The system shall verify that all resources have been deleted by checking AWS resource counts
- The system shall report any resources that failed to delete due to dependencies or protections
- The system shall provide a summary of destroyed resources in workflow output

**Proof Artifacts:**
- GitHub Actions workflow file: `.github/workflows/destroy-infrastructure.yml` demonstrates destruction workflow exists
- Workflow input screenshot: Manual workflow trigger showing confirmation inputs demonstrates safety controls
- Terraform destroy output: `terraform destroy` success message showing deleted resource count demonstrates infrastructure removal
- ECR cleanup logs: Workflow output showing deleted images demonstrates ECR cleanup
- AWS verification: `aws ecs describe-clusters` returning ResourceNotFoundException demonstrates cluster deletion
- Cost verification: AWS Cost Explorer showing reduced spend after destruction demonstrates resource cleanup

## Non-Goals (Out of Scope)

1. **Multi-environment deployments**: This spec focuses on a single staging environment only. Production or multiple environment workflows are out of scope.
2. **Blue-green or canary deployments**: Advanced deployment strategies are not included. Only basic rolling deployments with ECS.
3. **GitOps approach**: Infrastructure changes will be applied via workflows, not through GitOps tooling like ArgoCD or Flux.
4. **Container image scanning**: Security scanning of Docker images (e.g., Trivy, Snyk) is not included in initial implementation.
5. **Infrastructure cost estimation**: Tools like Infracost for cost analysis are explicitly excluded per user requirements.
6. **Slack/email notifications**: Notifications beyond GitHub Actions status are out of scope.
7. **Terraform drift detection**: Scheduled drift detection jobs are not included.
8. **GitHub OIDC authentication**: Using GitHub's OIDC provider for AWS authentication is deferred; using GitHub Secrets instead.

## Design Considerations

No specific design requirements identified. This is a backend infrastructure automation feature with no user interface beyond GitHub Actions workflow UI.

## Repository Standards

The implementation shall follow existing repository patterns:

**Workflow Conventions:**
- Use GitHub Actions workflow syntax version 2
- Follow existing workflow structure from `.github/workflows/e2e-tests.yml`
- Use standard GitHub Actions: `actions/checkout@v4`, `actions/setup-java@v4`, `docker/setup-buildx-action@v3`
- Name jobs and steps descriptively for clarity in GitHub Actions UI

**Commit Conventions:**
- Use conventional commit format: `feat:`, `fix:`, `ci:`, `docs:`
- Include co-author tag: `Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>`
- Keep commits atomic and focused on single changes

**Terraform Standards:**
- Use existing Terraform structure in `terraform/staging/` directory
- Follow naming convention: `petclinic-{environment}-{resource-type}-mumford`
- Run `terraform fmt` before committing configuration changes
- Use S3 backend with DynamoDB locking (already configured in `backend.tf`)

**Docker Standards:**
- Build images with `--platform linux/amd64` for ECS Fargate compatibility
- Use multi-stage builds following existing `Dockerfile` pattern
- Tag images with semantic versions from git tags

**Documentation:**
- Document workflow inputs and outputs in workflow file comments
- Add README section explaining how to trigger manual workflows
- Include proof artifacts in `docs/specs/03-spec-cicd-pipeline/03-proofs/` directory

## Technical Considerations

**AWS Authentication:**
- GitHub Actions workflows will authenticate to AWS using GitHub Secrets
- Required secrets: `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_REGION`
- IAM user/role must have permissions for: ECR (push/pull), ECS (update service, describe tasks), S3/DynamoDB (Terraform state)

**Terraform State Management:**
- Terraform state stored in AWS S3 backend with DynamoDB locking (already configured)
- Backend configuration in `terraform/staging/backend.tf` specifies state location
- Multiple pipeline runs must handle state locking gracefully (DynamoDB prevents conflicts)

**Docker Image Tagging:**
- Primary strategy: Semantic versioning from git tags (e.g., `v1.0.0`)
- Pipeline detects git tags on push events to trigger versioned builds
- Latest commit on main without tag will use commit SHA as fallback tag

**ECS Deployment Strategy:**
- Use `aws ecs update-service --force-new-deployment` to trigger task replacement
- Wait for deployment to stabilize before running health checks
- Monitor deployment status with `aws ecs describe-services` until runningCount equals desiredCount

**Rollback Mechanism:**
- On health check or E2E test failure, retrieve previous task definition revision
- Update ECS service to use previous task definition: `aws ecs update-service --task-definition <previous-revision>`
- Verify rollback completed successfully before failing workflow

**Database Migrations:**
- Run migrations before ECS deployment using a temporary container
- Use Spring Boot's built-in migration support or Flyway if configured
- Ensure migrations are idempotent to handle retries safely

**Testing Integration:**
- Unit tests run via `./mvnw test` before Docker build
- E2E tests run against deployed environment using existing Playwright tests in `e2e-tests/`
- E2E tests target ALB DNS endpoint retrieved from Terraform outputs

**Concurrency Control:**
- Use GitHub Actions concurrency groups to prevent multiple deployments to same environment
- Cancel in-progress deployments when new push occurs to same branch

## Security Considerations

**Secrets Management:**
- Store all sensitive credentials in GitHub Secrets (AWS credentials, database passwords if needed)
- Never commit credentials to repository or include in workflow logs
- Use GitHub's secret masking to prevent accidental exposure in logs
- Limit secret access to specific workflows using environment protection rules if possible

**AWS Credentials:**
- Create dedicated IAM user for GitHub Actions with least privilege permissions
- IAM user permissions limited to: ECR (read/write), ECS (update service), S3/DynamoDB (Terraform state), IAM (read for verification)
- Consider rotating AWS access keys periodically
- Use separate IAM users for different environments (staging vs production) if implemented

**Proof Artifact Security:**
- Redact sensitive information from proof artifacts (AWS account IDs, database endpoints)
- Do not commit AWS credentials or secrets to proof artifact files
- Screenshots should not reveal sensitive configuration values

**Workflow Security:**
- Limit manual workflow triggers to repository collaborators with write access
- Require confirmation inputs on destructive workflows (destroy)
- Use workflow approval gates if deploying to production environments in future

**Container Security:**
- Build Docker images from trusted base images (eclipse-temurin)
- Run containers as non-root user (already configured in Dockerfile)
- Keep base images updated to receive security patches

## Success Metrics

1. **Deployment Frequency**: Automated deployments occur within 10 minutes of pushing to main branch (measured by GitHub Actions workflow duration)
2. **Deployment Success Rate**: >95% of deployments complete successfully without manual intervention (measured by workflow success/failure ratio)
3. **Rollback Effectiveness**: Failed deployments automatically roll back within 5 minutes, maintaining application availability (measured by workflow rollback step timing)
4. **Infrastructure Initialization Time**: Manual initialization workflow completes full infrastructure setup in <15 minutes (measured by workflow run time)
5. **Developer Productivity**: Developers can see their code changes live in staging within 15 minutes of merge (measured by time from merge to healthy deployment)

## Open Questions

No open questions at this time. All requirements have been clarified through the questions round.
