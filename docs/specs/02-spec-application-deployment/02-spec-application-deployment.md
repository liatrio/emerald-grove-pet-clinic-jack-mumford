# 02-spec-application-deployment.md

## Introduction/Overview

This specification defines the deployment of the Spring Boot Pet Clinic application to AWS using ECS Fargate with an Application Load Balancer. The application will run as containerized tasks in the ECS cluster, connecting to the RDS PostgreSQL database provisioned in Spec 01. This spec focuses on the infrastructure components (Dockerfile, ECR repository, ECS cluster/service, ALB, IAM roles) required for a production-ready staging environment, with CI/CD automation deferred to Spec 03.

## Goals

- Deploy Pet Clinic application as containerized ECS Fargate tasks with 0.5 vCPU and 1024 MB memory
- Establish Application Load Balancer with HTTP access on port 80 for public internet traffic
- Configure auto-scaling based on CPU (70%) and memory (80%) utilization with 2 desired tasks (min 1, max 4)
- Integrate with AWS Secrets Manager for secure database credential retrieval
- Enable CloudWatch Logs for application observability and AWS X-Ray for distributed tracing
- Implement cost optimization with scheduled scaling to 0 tasks during off-hours

## User Stories

- **As a developer**, I want to containerize the Pet Clinic application using Alpine Linux with Java 17 so that I can deploy it efficiently to AWS ECS Fargate with minimal image size.

- **As a platform engineer**, I want to configure ECS Fargate with auto-scaling and health checks so that the application maintains high availability and automatically scales based on load.

- **As an end user**, I want to access the Pet Clinic application via a public URL so that I can manage pet records and appointments from anywhere.

- **As a DevOps engineer**, I want the application to retrieve database credentials from AWS Secrets Manager so that sensitive information is never hardcoded in the application or infrastructure code.

- **As a cost-conscious team member**, I want the staging environment to scale down to 0 tasks during off-hours so that we minimize AWS costs when the application is not actively used.

## Demoable Units of Work

### Unit 1: Container Image and ECR Repository

**Purpose:** Create a production-ready Docker image for the Pet Clinic application and establish an ECR repository for image storage, enabling deployment to ECS Fargate.

**Functional Requirements:**
- The system shall provide a Dockerfile using `openjdk:17-alpine` base image with multi-stage build for Maven compilation
- The Dockerfile shall expose port 8080 for the Spring Boot application
- The system shall include environment variable configuration for database connection via SPRING_DATASOURCE_* variables
- The system shall configure Spring Boot to use the `postgres` profile and retrieve credentials from environment variables
- Terraform shall create an ECR repository named `petclinic-staging-ecr-mumford` with lifecycle policy to retain last 5 images
- The Dockerfile shall copy the application JAR and run it using `java -jar` with appropriate JVM memory settings
- The system shall document the manual Docker build and push process (automation in Spec 03)

**Proof Artifacts:**
- File: `Dockerfile` in repository root demonstrates container configuration exists
- File: `terraform/staging/ecr.tf` with ECR repository resource demonstrates registry infrastructure
- CLI output: `terraform apply` showing ECR repository creation demonstrates infrastructure provisioning
- CLI output: `docker build -t petclinic:latest .` successful build demonstrates container builds correctly
- CLI output: `aws ecr describe-repositories --repository-names petclinic-staging-ecr-mumford` demonstrates repository exists
- Documentation: Build and push instructions in `terraform/README.md` demonstrates manual deployment process

### Unit 2: ECS Cluster and Task Definition

**Purpose:** Provision ECS Fargate cluster and task definition with proper resource allocation, IAM roles, and environment configuration for running the containerized application.

**Functional Requirements:**
- Terraform shall create an ECS cluster named `petclinic-staging-cluster-mumford`
- The system shall create an ECS task definition with Fargate launch type requiring 0.5 vCPU (512 CPU units) and 1024 MB memory
- The task definition shall reference the ECR image URI for the Pet Clinic container with port 8080 mapping
- The system shall create an IAM task execution role with permissions for ECR image pull and CloudWatch Logs write
- The system shall create an IAM task role with permissions for Secrets Manager read access, CloudWatch Logs, and X-Ray tracing
- The task definition shall configure environment variables: SPRING_PROFILES_ACTIVE=postgres, JAVA_OPTS=-Xmx768m
- The task definition shall configure secrets from AWS Secrets Manager for database credentials (SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD)
- The system shall create a CloudWatch Log Group named `/ecs/petclinic-staging-mumford` with 7-day retention
- The task definition shall configure AWS X-Ray daemon sidecar container for distributed tracing

**Proof Artifacts:**
- File: `terraform/staging/ecs.tf` with cluster and task definition demonstrates ECS infrastructure
- File: `terraform/staging/iam.tf` with task execution and task roles demonstrates IAM configuration
- CLI output: `terraform apply` showing ECS cluster and task definition creation demonstrates provisioning
- CLI output: `aws ecs describe-clusters --clusters petclinic-staging-cluster-mumford` demonstrates cluster exists
- CLI output: `aws ecs describe-task-definition --task-definition petclinic-staging-task-mumford` demonstrates task configuration
- CLI output: Task definition JSON showing environment variables and secrets demonstrates correct configuration

### Unit 3: Application Load Balancer and Target Group

**Purpose:** Deploy an internet-facing Application Load Balancer in public subnets with health checks to route HTTP traffic to ECS tasks.

**Functional Requirements:**
- Terraform shall create an Application Load Balancer named `petclinic-staging-alb-mumford` in public subnet(s)
- The ALB shall be internet-facing with security group allowing HTTP (port 80) from 0.0.0.0/0
- The system shall create a target group named `petclinic-staging-tg-mumford` with target type `ip` for Fargate tasks
- The target group shall forward traffic to port 8080 and use HTTP protocol
- The target group shall configure health checks on `/actuator/health` endpoint with 30-second interval and 2 consecutive successes for healthy status
- The target group shall use 5-second timeout and consider targets unhealthy after 3 consecutive failures
- The system shall create an HTTP listener on port 80 forwarding to the target group
- The ALB shall use the existing `petclinic-staging-alb-sg-mumford` security group from Spec 01
- The target group shall configure deregistration delay of 30 seconds for graceful shutdown

**Proof Artifacts:**
- File: `terraform/staging/alb.tf` with ALB, target group, and listener resources demonstrates load balancer infrastructure
- CLI output: `terraform apply` showing ALB and target group creation demonstrates provisioning
- CLI output: `aws elbv2 describe-load-balancers --names petclinic-staging-alb-mumford` demonstrates ALB exists
- CLI output: `aws elbv2 describe-target-groups --names petclinic-staging-tg-mumford` demonstrates target group configuration
- CLI output: `aws elbv2 describe-target-health --target-group-arn <arn>` demonstrates health check status
- Browser screenshot: ALB DNS name showing "target group has no registered targets" message demonstrates ALB is accessible (before service deployment)

### Unit 4: ECS Service with Auto-Scaling

**Purpose:** Deploy the ECS service with 2 tasks running in private subnets, configured with auto-scaling policies and registered with the Application Load Balancer.

**Functional Requirements:**
- Terraform shall create an ECS service named `petclinic-staging-service-mumford` using Fargate launch type
- The service shall run 2 desired tasks with minimum 1 and maximum 4 tasks for auto-scaling
- The service shall deploy tasks in private subnets with the application security group `petclinic-staging-app-sg-mumford`
- The service shall assign public IPs to tasks for ECR image pull (required for Fargate in private subnets with NAT Gateway)
- The service shall register tasks with the ALB target group automatically
- The service shall use "recreate" deployment strategy (stop all tasks, start new ones)
- The system shall configure Application Auto Scaling target tracking policies for CPU utilization (70% target) and memory utilization (80% target)
- The service shall configure health check grace period of 60 seconds to allow application startup
- The system shall configure scheduled scaling actions to scale down to 0 tasks during off-hours (defined by user schedule)
- The service shall depend on the ALB listener to ensure proper creation order

**Proof Artifacts:**
- File: `terraform/staging/ecs.tf` updated with ECS service and auto-scaling resources demonstrates service configuration
- CLI output: `terraform apply` showing ECS service creation demonstrates provisioning
- CLI output: `aws ecs describe-services --cluster petclinic-staging-cluster-mumford --services petclinic-staging-service-mumford` demonstrates service exists and shows desired count
- CLI output: `aws ecs list-tasks --cluster petclinic-staging-cluster-mumford --service-name petclinic-staging-service-mumford` demonstrates tasks are running
- CLI output: `aws application-autoscaling describe-scaling-policies --service-namespace ecs` demonstrates auto-scaling policies exist
- AWS Console screenshot: ECS service showing 2 running tasks demonstrates service is operational
- AWS Console screenshot: ALB target group showing 2 healthy targets demonstrates tasks are registered and passing health checks

### Unit 5: End-to-End Application Access and Validation

**Purpose:** Validate that the Pet Clinic application is fully operational, accessible via the ALB, successfully connected to the RDS database, and logging to CloudWatch.

**Functional Requirements:**
- The user shall be able to access the Pet Clinic homepage via the ALB DNS name using HTTP on port 80
- The application shall successfully retrieve database credentials from AWS Secrets Manager on startup
- The application shall connect to the RDS PostgreSQL database using credentials from Secrets Manager
- The application shall serve the homepage with Liatrio branding and navigation links (Owners, Veterinarians, etc.)
- The system shall log application startup messages and database connection events to CloudWatch Logs
- The application shall respond to health check requests at `/actuator/health` with HTTP 200 status
- The ALB shall show all registered targets as "healthy" in the target group
- The user shall be able to navigate to different pages (e.g., /owners, /vets) and see data from the database
- CloudWatch Logs shall contain log streams from each ECS task with application output

**Proof Artifacts:**
- Browser screenshot: Pet Clinic homepage accessed via `http://<alb-dns-name>` demonstrates application is publicly accessible
- Browser screenshot: Owners page showing database records demonstrates database connectivity
- Browser screenshot: Veterinarians page showing vet data demonstrates full application functionality
- CLI output: `curl http://<alb-dns-name>/actuator/health` returning `{"status":"UP"}` demonstrates health endpoint works
- CLI output: `aws logs tail /ecs/petclinic-staging-mumford --follow` showing application logs demonstrates CloudWatch logging
- CloudWatch Logs screenshot: Log stream showing "Started PetClinicApplication" and database connection success demonstrates application startup
- AWS Console screenshot: ALB target group showing all targets healthy demonstrates health checks passing
- CLI output: `aws ecs describe-tasks --cluster petclinic-staging-cluster-mumford --tasks <task-id>` showing RUNNING status demonstrates tasks are stable

## Non-Goals (Out of Scope)

1. **CI/CD Pipeline Automation**: Automated Docker image builds, pushes to ECR, and ECS service updates will be implemented in Spec 03. This spec focuses on infrastructure and manual deployment procedures.

2. **Database Schema Migrations**: Automated database schema initialization and migration workflows will be part of Spec 03. This spec assumes the database schema will be managed separately via GitHub workflows.

3. **HTTPS/SSL Configuration**: SSL certificate provisioning via ACM, HTTPS listeners, and HTTP-to-HTTPS redirects are deferred to a future spec. Staging environment uses HTTP only.

4. **Custom Domain Names**: Route 53 hosted zone setup, DNS records, and custom domain configuration are out of scope. Application will be accessed via ALB DNS name only.

5. **Blue-Green or Canary Deployments**: Advanced deployment strategies with AWS CodeDeploy are out of scope. Service uses "recreate" strategy for simplicity.

6. **Multi-Region Deployment**: This spec covers us-east-1 staging environment only. Multi-region or disaster recovery configurations are not included.

7. **WAF and DDoS Protection**: AWS WAF, Shield, and advanced security features are out of scope for staging. Production security will be addressed separately.

8. **Fargate Spot Integration**: While cost optimization via scheduled scaling is included, Fargate Spot pricing is deferred to optimize staging costs further if needed.

## Design Considerations

**Application Interface:**
- Use existing Liatrio-branded Pet Clinic UI with Bootstrap 5 styling
- No UI changes required; application renders server-side HTML via Thymeleaf templates
- Responsive design works on mobile and desktop browsers
- Navigation includes: Home, Owners, Veterinarians, Find Owners, Error page

**Health Check Endpoint:**
- Spring Boot Actuator provides `/actuator/health` endpoint automatically
- Actuator dependency already included in Pet Clinic application
- Health endpoint returns JSON: `{"status":"UP"}` when application is healthy
- ALB health checks parse HTTP 200 response as "healthy"

**No Specific Design Mockups Required:**
- Application UI is pre-existing and unchanged
- Focus is on infrastructure deployment, not UI/UX modifications

## Repository Standards

**Terraform Standards:**
- Follow existing Terraform structure from Spec 01 (`terraform/staging/` directory)
- Use consistent naming: `petclinic-{environment}-{resource-type}-mumford`
- Format all `.tf` files with `terraform fmt` before committing
- Add comments explaining resource purposes
- Use variables for configurable values (task CPU, memory, desired count, etc.)
- Commit Terraform code separately from documentation (per AGENTS.md)

**Docker Standards:**
- Place `Dockerfile` in repository root for Maven project layout compatibility
- Use multi-stage builds to separate build and runtime stages
- Minimize image size using Alpine Linux base image
- Follow Docker best practices (non-root user, explicit EXPOSE, HEALTHCHECK)
- Add `.dockerignore` to exclude unnecessary files from build context

**Application Configuration:**
- Use Spring profiles for environment-specific configuration (`postgres` profile)
- Configure via environment variables (12-factor app principles)
- Never hardcode credentials or secrets in application code or configuration files
- Retrieve sensitive values from AWS Secrets Manager at runtime

**Commit Conventions:**
- Use conventional commit format: `feat:`, `fix:`, `chore:`, `docs:`
- Include co-author tag: `Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>`
- Keep commits atomic and focused on single changes
- Commit frequently (per AGENTS.md guidelines)

**Testing Standards:**
- While this spec focuses on infrastructure, maintain existing test suite
- Ensure application builds successfully before containerization
- Validate Docker image locally before pushing to ECR
- Verify application starts and connects to database in container

## Technical Considerations

**ECS Fargate Configuration:**
- Use Fargate platform version 1.4.0 or later for best performance and features
- Tasks in private subnets require NAT Gateway for ECR image pulls and internet access
- Assign public IPs to Fargate tasks in private subnets when using NAT Gateway
- Use `awsvpc` network mode (required for Fargate)
- Configure task CPU and memory in Fargate-supported combinations (0.5 vCPU + 1024 MB)

**Database Connection:**
- Construct JDBC URL from Secrets Manager values: `jdbc:postgresql://${host}:${port}/${dbname}`
- Use Spring Boot's built-in Secrets Manager integration or fetch via ECS task definition secrets
- Environment variables: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
- HikariCP connection pool (Spring Boot default) handles connection management automatically
- Set JVM max heap to 768 MB (75% of 1024 MB task memory) to prevent OOM errors

**Application Load Balancer:**
- ALB must be in public subnets for internet access
- Target type must be `ip` for Fargate tasks (not `instance`)
- Health check path `/actuator/health` requires Spring Boot Actuator dependency
- Configure health check interval, timeout, and thresholds appropriately for app startup time
- Deregistration delay allows in-flight requests to complete before task shutdown

**Auto-Scaling:**
- Target tracking scaling policies automatically create CloudWatch alarms
- Scaling policies should use ECS service metrics (CPUUtilization, MemoryUtilization)
- Scale-out happens faster than scale-in to handle traffic spikes
- Scheduled scaling requires Application Auto Scaling scheduled actions with min/max/desired capacity

**IAM Roles:**
- **Task Execution Role**: Used by ECS to pull images from ECR and write logs to CloudWatch (AWS-managed role principles)
- **Task Role**: Used by application code to access AWS services (Secrets Manager, X-Ray, CloudWatch)
- Follow least privilege principle: grant only required permissions
- Use managed policies where appropriate (`SecretsManagerReadWrite`, `CloudWatchLogsFullAccess`, `AWSXRayDaemonWriteAccess`)

**CloudWatch Logs:**
- Log group name: `/ecs/petclinic-staging-mumford` (consistent naming)
- Retention period: 7 days for staging (cost optimization)
- Each ECS task creates a unique log stream
- Use `awslogs` log driver in task definition (Fargate default)

**X-Ray Tracing:**
- X-Ray daemon runs as sidecar container in task definition
- Application code must include X-Ray SDK for tracing (Spring Boot X-Ray integration)
- X-Ray provides distributed tracing and performance insights
- X-Ray daemon requires IAM permissions to write trace data

**Cost Optimization:**
- Scheduled scaling to 0 tasks during off-hours (e.g., 10 PM - 6 AM EST weekdays, weekends)
- Use Application Auto Scaling scheduled actions with cron expressions
- Monitor costs using AWS Cost Explorer and set up billing alarms
- Consider Fargate Spot in future for additional savings (not in this spec)

**Dependencies:**
- Spec 01 infrastructure must be deployed (VPC, subnets, security groups, RDS, Secrets Manager)
- Docker must be installed locally for manual image builds
- AWS CLI must be configured with appropriate permissions
- ECR authentication required: `aws ecr get-login-password | docker login --username AWS --password-stdin <ecr-uri>`

## Security Considerations

**Secrets Management:**
- Database credentials stored in AWS Secrets Manager, never in code or environment variable values in Terraform
- ECS task definition references Secrets Manager secrets by ARN, not plaintext values
- IAM task role grants read-only access to specific secret: `petclinic/staging/database`
- Secrets Manager encrypts secrets at rest using AWS-managed KMS keys

**Network Security:**
- ECS tasks run in private subnets, not directly accessible from internet
- ALB in public subnet provides controlled access point
- Security group rules enforce least privilege (ALB → App on 8080, App → RDS on 5432)
- No SSH access to Fargate tasks (serverless, no EC2 instances)

**Container Security:**
- Use official Alpine Linux and OpenJDK base images from trusted sources
- Scan Docker images for vulnerabilities using `docker scan` or ECR image scanning
- Run container as non-root user in Dockerfile
- Avoid including secrets in Docker image layers

**IAM Security:**
- Task execution role limited to ECR pull and CloudWatch Logs write
- Task role limited to Secrets Manager read, CloudWatch Logs, and X-Ray write
- Use IAM resource-based policies to restrict access to specific resources
- Enable CloudTrail logging for IAM API calls

**Proof Artifacts Security:**
- Redact sensitive information (passwords, AWS account IDs) from screenshots and CLI output
- Do not commit database credentials or API keys to version control
- Use placeholder values in documentation examples
- Store proof artifacts in `docs/specs/02-spec-application-deployment/02-proofs/` directory

**Application Security:**
- Spring Boot Actuator health endpoint is read-only, does not expose sensitive information
- Configure Spring Security if authentication is required (not in this spec)
- Use HTTPS in production (deferred to future spec for staging)
- Follow OWASP top 10 security practices in application code

## Success Metrics

1. **Deployment Success Rate**: 100% of Terraform apply operations succeed without manual intervention for infrastructure provisioning.

2. **Application Availability**: Pet Clinic application responds to HTTP requests via ALB DNS name with <2 second response time for homepage.

3. **Health Check Success**: ALB target group shows all registered ECS tasks as "healthy" with `/actuator/health` endpoint returning HTTP 200.

4. **Database Connectivity**: Application successfully connects to RDS PostgreSQL database using credentials from Secrets Manager within 30 seconds of task startup.

5. **Auto-Scaling Responsiveness**: ECS service scales from 2 to 3 tasks within 5 minutes when CPU utilization exceeds 70% for 3 consecutive minutes (simulated load test).

6. **Cost Optimization**: Scheduled scaling reduces task count to 0 during configured off-hours, reducing compute costs by ~50% for staging environment.

7. **Log Availability**: CloudWatch Logs contains application logs from all ECS tasks within 1 minute of log generation, queryable via AWS Console or CLI.

8. **Infrastructure as Code Coverage**: 100% of AWS resources (ECR, ECS, ALB, IAM, CloudWatch) defined in Terraform with no manual console configurations.

## Open Questions

1. **Scheduled Scaling Times**: What specific cron schedule should be used for scaling down to 0 tasks during off-hours? (e.g., "Scale to 0 at 10 PM EST, scale to 2 at 6 AM EST on weekdays")?

2. **X-Ray Integration**: Does the Pet Clinic application currently include AWS X-Ray SDK dependencies, or should this be added as part of this spec?

3. **Spring Boot Actuator**: Is the Actuator dependency already included in the Pet Clinic `pom.xml`, or does it need to be added?

4. **Database Schema State**: Should we assume the database schema already exists from Spec 01, or document the GitHub workflow approach for initial schema creation in this spec?

5. **ECR Image Tagging Strategy**: What tagging convention should be used for Docker images in ECR? (e.g., `latest`, `git-commit-sha`, `v1.0.0`, `staging-YYYY-MM-DD`)?

6. **Terraform State**: Should the ECS/ALB resources use the same Terraform state file as Spec 01, or separate state management?
