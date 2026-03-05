# 02 Questions Round 1 - Application Deployment

Please answer each question below (select one or more options, or add your own notes). Feel free to add additional context under any question.

## 1. Container Build Strategy

How should we build and manage the Docker container for the Pet Clinic application?

- [ ] (A) Create Dockerfile in repository root, build locally and push to ECR manually
- [ ] (B) Create Dockerfile in repository root, use Terraform to create ECR repository, document build/push process
- [x] (C) Use CI/CD pipeline to automate Docker build and push (will be part of Spec 03)
- [ ] (D) Use AWS CodeBuild to build container from source
- [ ] (E) Other (describe)

## 2. Container Base Image

What base image should we use for the Spring Boot application?

- [ ] (A) Eclipse Temurin (formerly AdoptOpenJDK) official Java 17 image (e.g., `eclipse-temurin:17-jre`)
- [ ] (B) Amazon Corretto 17 (AWS's OpenJDK distribution)
- [ ] (C) Official OpenJDK 17 image
- [x] (D) Alpine Linux with Java 17 (smaller image size)
- [ ] (E) Other (describe)

## 3. ECS Task Resource Allocation

What CPU and memory configuration should we use for the ECS Fargate tasks?

- [ ] (A) 0.25 vCPU, 512 MB memory (smallest, cost-optimized for staging)
- [x] (B) 0.5 vCPU, 1024 MB memory (recommended for Spring Boot apps)
- [ ] (C) 1 vCPU, 2048 MB memory (more headroom for performance)
- [ ] (D) 2 vCPU, 4096 MB memory (production-grade)
- [ ] (E) Other (describe)

## 4. ECS Service Scaling Configuration

What should be the initial task count and auto-scaling parameters?

- [ ] (A) Desired: 1, Min: 1, Max: 2 (minimal staging setup)
- [x] (B) Desired: 2, Min: 1, Max: 4 (recommended for HA)
- [ ] (C) Desired: 2, Min: 2, Max: 6 (always multi-AZ)
- [ ] (D) Desired: 3, Min: 2, Max: 10 (production-grade)
- [ ] (E) Other (describe)

## 5. Auto-Scaling Metrics

What metrics should trigger auto-scaling of ECS tasks?

- [ ] (A) CPU utilization only (target 70%)
- [ ] (B) Memory utilization only (target 80%)
- [x] (C) Both CPU (70%) and memory (80%) utilization
- [ ] (D) Request count per target (ALB metric)
- [ ] (E) Other (describe)

## 6. Application Load Balancer Configuration

What listener configuration should the ALB use?

- [x] (A) HTTP only on port 80 (staging, no SSL)
- [ ] (B) HTTP on port 80, redirect to HTTPS on port 443
- [ ] (C) HTTPS only on port 443 (requires SSL certificate)
- [ ] (D) Both HTTP (80) and HTTPS (443) without redirect
- [ ] (E) Other (describe)

## 7. SSL/TLS Certificate

Do you have an SSL certificate or domain name for HTTPS access?

- [x] (A) No SSL certificate, use HTTP only for staging
- [ ] (B) Use AWS Certificate Manager (ACM) to create certificate (requires domain validation)
- [ ] (C) I have an existing ACM certificate ARN
- [ ] (D) Import external certificate to ACM
- [ ] (E) Other (describe)

**If you selected B or C, please provide the domain name:**

## 8. Health Check Configuration

What endpoint should the ALB use for health checks?

- [ ] (A) `/` (root path, simple availability check)
- [x] (B) `/actuator/health` (Spring Boot Actuator endpoint with detailed health info)
- [ ] (C) Custom `/health` endpoint (need to implement)
- [ ] (D) `/actuator/health/liveness` (Kubernetes-style liveness probe)
- [ ] (E) Other (describe)

## 9. Database Migration Strategy

How should the database schema be initialized on first deployment?

- [ ] (A) Spring Boot auto-creates schema on startup (spring.jpa.hibernate.ddl-auto=update)
- [ ] (B) Use Flyway or Liquibase for versioned migrations (need to set up)
- [ ] (C) Manually run SQL scripts before deployment
- [ ] (D) Use Terraform to initialize schema (less common)
- [x] (E) Other (describe) The whole infrastruction should be handled within a GitHub workflow. For first time deployment there should be a separate workflow to create this. Then every time we want to deploy a new version of the application we should run a different workflow.

## 10. Environment Configuration

How should environment-specific configuration be managed?

- [x] (A) Environment variables passed to ECS task definition (DATABASE_URL, etc.)
- [ ] (B) AWS Systems Manager Parameter Store for configuration
- [ ] (C) Mount configuration file from S3
- [ ] (D) Bake configuration into Docker image (not recommended)
- [ ] (E) Other (describe)

## 11. Logging Strategy

Where should application logs be sent?

- [x] (A) CloudWatch Logs (ECS default, recommended)
- [ ] (B) CloudWatch Logs with custom log group naming
- [ ] (C) External logging service (Datadog, Splunk, etc.)
- [ ] (D) S3 bucket for long-term storage
- [ ] (E) Other (describe)

## 12. Database Connection Pooling

What connection pooling configuration should we use?

- [x] (A) Use HikariCP defaults (Spring Boot default, typically 10 connections)
- [ ] (B) Tune HikariCP for low-resource environment (max 5 connections)
- [ ] (C) Tune HikariCP for high-concurrency (max 20+ connections)
- [ ] (D) Configure based on RDS max_connections setting
- [ ] (E) Other (describe)

## 13. Container Port Mapping

The Spring Boot app runs on port 8080. What port should the container expose?

- [x] (A) Keep port 8080 (container port 8080, ALB target group uses 8080)
- [ ] (B) Change to port 80 (modify app to run on 80, requires privilege)
- [ ] (C) Use port 8080 in container, ALB translates to 80/443 for external access
- [ ] (D) Other (describe)

## 14. ECS Service Deployment Strategy

What deployment strategy should ECS use when updating the service?

- [ ] (A) Rolling update with 100% minimum healthy (zero downtime, requires 2x capacity temporarily)
- [ ] (B) Rolling update with 50% minimum healthy (faster deployment, brief capacity reduction)
- [ ] (C) Blue-green deployment with CodeDeploy (more complex, cleanest rollback)
- [x] (D) Recreate strategy (stop all tasks, start new ones - brief downtime)
- [ ] (E) Other (describe)

## 15. IAM Permissions for ECS Tasks

What AWS permissions does the ECS task need?

- [ ] (A) Read-only access to Secrets Manager for database credentials
- [ ] (B) Secrets Manager + S3 read access (for application assets/config)
- [ ] (C) Secrets Manager + S3 read/write (for file uploads)
- [x] (D) Secrets Manager + CloudWatch Logs + X-Ray (comprehensive observability)
- [ ] (E) Other (describe)

## 16. Public Access

Should the application be publicly accessible via the ALB?

- [x] (A) Yes, ALB in public subnet, accessible from internet (0.0.0.0/0)
- [ ] (B) Yes, but restrict to specific IP ranges (office IP, VPN, etc.)
- [ ] (C) No, ALB in private subnet, accessible only within VPC
- [ ] (D) Use AWS WAF for additional protection (recommended for production)
- [ ] (E) Other (describe)

**If you selected B, please provide the allowed IP ranges/CIDR blocks:**

## 17. Domain Name and DNS

Do you want to configure a custom domain name for the application?

- [x] (A) No custom domain, use ALB DNS name only (e.g., petclinic-staging-alb-mumford-123456.us-east-1.elb.amazonaws.com)
- [ ] (B) Yes, configure Route 53 alias record to ALB (requires hosted zone)
- [ ] (C) Yes, configure CNAME record in external DNS provider
- [ ] (D) Defer to later spec (DNS/domain management)
- [ ] (E) Other (describe)

**If you selected B or C, please provide the desired domain/subdomain:**

## 18. Cost Optimization

What cost optimization strategies should we implement for staging?

- [ ] (A) Use Fargate Spot for non-critical tasks (up to 70% cost savings, may be interrupted)
- [ ] (B) Use standard Fargate pricing (reliable, predictable)
- [x] (C) Scale down to 0 tasks during off-hours (save on compute, requires scheduled scaling)
- [ ] (D) Use Savings Plans or Reserved Instances (longer-term commitment)
- [ ] (E) Other (describe)

## 19. Proof Artifacts

What proof artifacts should demonstrate successful deployment?

- [ ] (A) Browser screenshot showing application homepage accessible via ALB DNS
- [ ] (B) CLI output showing ECS service running with desired task count
- [ ] (C) CloudWatch Logs showing application startup and database connection
- [ ] (D) AWS Console screenshots of ECS service, tasks, and ALB target health
- [x] (E) All of the above

## 20. Terraform Organization

How should the Terraform code be organized?

- [x] (A) Add ECS resources to existing `terraform/staging/` directory (new .tf files)
- [ ] (B) Create separate `terraform/ecs/` directory for container infrastructure
- [ ] (C) Create `terraform/staging/ecs.tf`, `terraform/staging/alb.tf`, `terraform/staging/iam.tf`
- [ ] (D) Keep all in single `terraform/staging/application.tf` file
- [ ] (E) Other (describe)
