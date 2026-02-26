# 02-tasks-application-deployment.md

## Overview

This task list implements **Spec 02: Application Deployment** for deploying the Spring Boot Pet Clinic application to AWS ECS Fargate with an Application Load Balancer. The implementation builds on the core infrastructure from Spec 01 (VPC, subnets, security groups, RDS, Secrets Manager) and adds container orchestration, load balancing, and auto-scaling capabilities.

**Spec Reference:** `02-spec-application-deployment.md`

**Implementation Strategy:** Progressive deployment starting with containerization, then infrastructure provisioning (ECR, ECS, ALB), service deployment, and finally end-to-end validation with auto-scaling and monitoring.

---

## Relevant Files

### New Files to Create

- `Dockerfile` - Multi-stage Docker build configuration for Pet Clinic application with Alpine Linux and Java 17
- `.dockerignore` - Exclude unnecessary files from Docker build context
- `terraform/staging/ecr.tf` - ECR repository configuration with lifecycle policy
- `terraform/staging/ecs.tf` - ECS cluster, task definition, and service configuration
- `terraform/staging/iam.tf` - IAM roles for ECS task execution and task runtime
- `terraform/staging/alb.tf` - Application Load Balancer, target group, and listener configuration
- `docs/specs/02-spec-application-deployment/02-proofs/` - Directory for proof artifacts (screenshots, CLI outputs)

### Existing Files to Modify

- `terraform/staging/outputs.tf` - Add outputs for ALB DNS name, ECS cluster ARN, service name
- `terraform/staging/variables.tf` - Add variables for ECS task configuration (CPU, memory, desired count, scaling parameters)
- `terraform/README.md` - Add documentation for Docker build/push process and ECS deployment

### Files to Reference (No Modification)

- `pom.xml` - Maven project configuration (already includes Spring Boot Actuator dependency)
- `src/main/resources/application.properties` - Spring Boot configuration
- `src/main/resources/application-postgres.properties` - PostgreSQL profile configuration
- `terraform/staging/vpc.tf` - VPC and subnet references from Spec 01
- `terraform/staging/security_groups.tf` - Security group references from Spec 01
- `terraform/staging/secrets.tf` - Secrets Manager secret ARN reference from Spec 01

### Notes

- Follow existing Terraform structure and naming conventions from Spec 01
- Use `terraform fmt` before committing Terraform files
- Commit Terraform code separately from documentation (per AGENTS.md)
- Spring Boot Actuator is already included in the project (no dependency changes needed)
- Database credentials will be retrieved from Secrets Manager secret created in Spec 01: `petclinic/staging/database`

---

## Tasks

### [x] 1.0 Create Container Image Configuration and ECR Repository

Develop Dockerfile for containerizing the Pet Clinic application using Alpine Linux with Java 17, and provision ECR repository via Terraform for secure image storage.

#### 1.0 Proof Artifact(s)

- File: `Dockerfile` in repository root demonstrates container configuration exists
- File: `terraform/staging/ecr.tf` with ECR repository resource demonstrates registry infrastructure
- CLI output: `terraform apply` showing ECR repository creation demonstrates infrastructure provisioning
- CLI output: `docker build -t petclinic:latest .` successful build demonstrates container builds correctly
- CLI output: `aws ecr describe-repositories --repository-names petclinic-staging-ecr-mumford` demonstrates repository exists and shows lifecycle policy configuration
- Documentation: Build and push instructions in `terraform/README.md` demonstrates manual deployment process for staging

#### 1.0 Tasks

- [ ] 1.1 Create `Dockerfile` in repository root with multi-stage build: Stage 1 (Maven build) uses `maven:3.9-eclipse-temurin-17-alpine` to run `mvn clean package -DskipTests`, Stage 2 (runtime) uses `eclipse-temurin:17-jre-alpine` and copies JAR from Stage 1
- [ ] 1.2 Configure Dockerfile runtime stage: expose port 8080, set JAVA_OPTS environment variable with `-Xmx768m`, create non-root user `appuser`, run as `appuser`, use `ENTRYPOINT` with `java $JAVA_OPTS -jar /app/petclinic.jar`
- [ ] 1.3 Create `.dockerignore` file to exclude `target/`, `.git/`, `*.md`, `terraform/`, `docs/`, `.mvn/`, and other unnecessary files from Docker build context
- [ ] 1.4 Build Docker image locally: `docker build -t petclinic:latest .` to verify Dockerfile works correctly and application compiles
- [ ] 1.5 Test Docker image locally: `docker run --rm -p 8080:8080 -e SPRING_PROFILES_ACTIVE=h2 petclinic:latest` and verify application starts successfully (use H2 profile for local testing)
- [ ] 1.6 Create `terraform/staging/ecr.tf` file
- [ ] 1.7 Add ECR repository resource to `ecr.tf` with name `petclinic-staging-ecr-mumford`, image_tag_mutability `MUTABLE`, and image scanning on push enabled
- [ ] 1.8 Add ECR lifecycle policy to `ecr.tf` to retain only last 5 images (delete untagged images older than 1 day, keep last 5 tagged images)
- [ ] 1.9 Add variables to `terraform/staging/variables.tf`: `ecr_repository_name` (string, default "petclinic-staging-ecr-mumford")
- [ ] 1.10 Run `terraform fmt` and `terraform validate` from `terraform/staging/` directory to ensure ECR configuration is properly formatted and valid
- [ ] 1.11 Run `terraform plan` to review ECR repository changes before applying
- [ ] 1.12 Run `terraform apply` to create ECR repository
- [ ] 1.13 Verify ECR repository creation: `aws ecr describe-repositories --repository-names petclinic-staging-ecr-mumford --region us-east-1`
- [ ] 1.14 Get ECR repository URI: `aws ecr describe-repositories --repository-names petclinic-staging-ecr-mumford --query 'repositories[0].repositoryUri' --output text`
- [ ] 1.15 Authenticate Docker to ECR: `aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-1.amazonaws.com`
- [ ] 1.16 Tag Docker image for ECR: `docker tag petclinic:latest <ecr-uri>:latest`
- [ ] 1.17 Push Docker image to ECR: `docker push <ecr-uri>:latest` to verify manual push process works
- [ ] 1.18 Verify image in ECR: `aws ecr describe-images --repository-name petclinic-staging-ecr-mumford --region us-east-1`
- [ ] 1.19 Create proof artifacts directory: `mkdir -p docs/specs/02-spec-application-deployment/02-proofs`
- [ ] 1.20 Capture proof artifacts: save Docker build output, terraform apply output, ECR repository description, and ECR image list to proof file
- [ ] 1.21 Commit Dockerfile and .dockerignore with message: `feat: add Dockerfile with Alpine Java 17 multi-stage build`
- [ ] 1.22 Commit ECR Terraform configuration with message: `feat: add ECR repository with lifecycle policy`
- [ ] 1.23 Update `terraform/README.md` with Docker build/push instructions (new section: "Building and Pushing Docker Images")
- [ ] 1.24 Commit README documentation in **separate commit** with message: `docs: add Docker build and ECR push instructions`

---

### [x] 2.0 Provision ECS Cluster and Task Definition

Create ECS Fargate cluster with task definition configured for 0.5 vCPU and 1024 MB memory, including IAM roles, environment variables, Secrets Manager integration, CloudWatch logging, and X-Ray tracing.

#### 2.0 Proof Artifact(s)

- File: `terraform/staging/ecs.tf` with cluster and task definition demonstrates ECS infrastructure
- File: `terraform/staging/iam.tf` with task execution role and task role demonstrates IAM configuration
- CLI output: `terraform apply` showing ECS cluster and task definition creation demonstrates provisioning
- CLI output: `aws ecs describe-clusters --clusters petclinic-staging-cluster-mumford` demonstrates cluster exists and shows status
- CLI output: `aws ecs describe-task-definition --task-definition petclinic-staging-task-mumford:1` demonstrates task configuration with CPU, memory, and container definitions
- CLI output: Task definition JSON showing environment variables (SPRING_PROFILES_ACTIVE, JAVA_OPTS) and secrets configuration demonstrates correct application configuration
- CLI output: `aws logs describe-log-groups --log-group-name-prefix /ecs/petclinic-staging` demonstrates CloudWatch log group creation

#### 2.0 Tasks

- [ ] 2.1 Create `terraform/staging/ecs.tf` file with header comment describing ECS Fargate configuration
- [ ] 2.2 Add ECS cluster resource to `ecs.tf` with name `petclinic-staging-cluster-mumford` and container insights enabled
- [ ] 2.3 Add CloudWatch Log Group resource to `ecs.tf` with name `/ecs/petclinic-staging-mumford`, retention 7 days
- [ ] 2.4 Create `terraform/staging/iam.tf` file with header comment describing IAM roles for ECS tasks
- [ ] 2.5 Add IAM policy document data source for ECS task execution role assume role policy (trust relationship for `ecs-tasks.amazonaws.com`)
- [ ] 2.6 Add IAM task execution role resource with name `petclinic-staging-task-execution-role-mumford` and attach assume role policy
- [ ] 2.7 Attach AWS managed policy `AmazonECSTaskExecutionRolePolicy` to task execution role (allows ECR pull and CloudWatch Logs write)
- [ ] 2.8 Add IAM policy document data source for ECS task role assume role policy (trust relationship for `ecs-tasks.amazonaws.com`)
- [ ] 2.9 Add IAM task role resource with name `petclinic-staging-task-role-mumford` for application runtime permissions
- [ ] 2.10 Create inline IAM policy for task role allowing `secretsmanager:GetSecretValue` on the database secret ARN from Spec 01
- [ ] 2.11 Attach AWS managed policies to task role: `CloudWatchLogsFullAccess` and `AWSXRayDaemonWriteAccess` for observability
- [ ] 2.12 Add variables to `terraform/staging/variables.tf`: `ecs_task_cpu` (number, default 512), `ecs_task_memory` (number, default 1024), `ecs_container_port` (number, default 8080)
- [ ] 2.13 Get ECR repository URI using data source or output: `data "aws_ecr_repository" "petclinic"` or reference from ECR resource
- [ ] 2.14 Add ECS task definition resource to `ecs.tf` with family `petclinic-staging-task-mumford`, network_mode `awsvpc`, requires_compatibilities `["FARGATE"]`, cpu and memory from variables
- [ ] 2.15 Configure main container definition in task definition: name `petclinic`, image from ECR URI with `:latest` tag, port mapping 8080:8080
- [ ] 2.16 Add environment variables to container definition: `SPRING_PROFILES_ACTIVE=postgres`, `JAVA_OPTS=-Xmx768m -XX:+UseContainerSupport`
- [ ] 2.17 Add secrets to container definition from Secrets Manager: `SPRING_DATASOURCE_URL` (constructed from host/port/dbname), `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` using valueFrom with secret ARN and JSON keys
- [ ] 2.18 Configure CloudWatch Logs for container: log driver `awslogs`, log group `/ecs/petclinic-staging-mumford`, region `us-east-1`, stream prefix `ecs`
- [ ] 2.19 Add X-Ray daemon sidecar container to task definition: name `xray-daemon`, image `public.ecr.aws/xray/aws-xray-daemon:latest`, cpu 32, memory 256, port mapping 2000:2000/udp
- [ ] 2.20 Configure X-Ray daemon container logs to same CloudWatch log group with stream prefix `xray`
- [ ] 2.21 Set task execution role ARN and task role ARN in task definition
- [ ] 2.22 Run `terraform fmt` and `terraform validate` to ensure ECS and IAM configurations are valid
- [ ] 2.23 Run `terraform plan` to review ECS cluster, task definition, IAM roles, and log group changes
- [ ] 2.24 Run `terraform apply` to create ECS cluster, task definition, IAM roles, and CloudWatch log group
- [ ] 2.25 Verify ECS cluster: `aws ecs describe-clusters --clusters petclinic-staging-cluster-mumford --region us-east-1`
- [ ] 2.26 Verify task definition: `aws ecs describe-task-definition --task-definition petclinic-staging-task-mumford --region us-east-1`
- [ ] 2.27 Verify task definition JSON shows correct CPU (512), memory (1024), container definitions, environment variables, and secrets configuration
- [ ] 2.28 Verify CloudWatch log group: `aws logs describe-log-groups --log-group-name-prefix /ecs/petclinic-staging --region us-east-1`
- [ ] 2.29 Verify IAM roles exist: `aws iam get-role --role-name petclinic-staging-task-execution-role-mumford` and `aws iam get-role --role-name petclinic-staging-task-role-mumford`
- [ ] 2.30 Create proof artifacts documenting ECS cluster, task definition configuration, IAM roles, and CloudWatch log group
- [ ] 2.31 Commit ECS and IAM Terraform configuration with message: `feat: add ECS Fargate cluster and task definition with IAM roles`

---

### [x] 3.0 Deploy Application Load Balancer and Target Group

Provision internet-facing Application Load Balancer in public subnets with HTTP listener, target group configured for Fargate IP targets, and health checks on Spring Boot Actuator endpoint.

#### 3.0 Proof Artifact(s)

- File: `terraform/staging/alb.tf` with ALB, target group, and listener resources demonstrates load balancer infrastructure
- CLI output: `terraform apply` showing ALB and target group creation demonstrates provisioning
- CLI output: `aws elbv2 describe-load-balancers --names petclinic-staging-alb-mumford` demonstrates ALB exists and shows DNS name and state
- CLI output: `aws elbv2 describe-target-groups --names petclinic-staging-tg-mumford` demonstrates target group configuration with health check settings
- CLI output: `aws elbv2 describe-listeners --load-balancer-arn <arn>` demonstrates HTTP listener on port 80 forwarding to target group
- Browser screenshot: Accessing ALB DNS name (http://<alb-dns>) showing "503 Service Temporarily Unavailable" or "no registered targets" demonstrates ALB is accessible but waiting for ECS service registration

#### 3.0 Tasks

- [ ] 3.1 Create `terraform/staging/alb.tf` file with header comment describing Application Load Balancer configuration
- [ ] 3.2 Add Application Load Balancer resource to `alb.tf` with name `petclinic-staging-alb-mumford`, load_balancer_type `application`, internal `false` (internet-facing)
- [ ] 3.3 Configure ALB to use public subnet(s): reference `aws_subnet.public.id` from vpc.tf, enable deletion protection `false` for staging
- [ ] 3.4 Configure ALB security group: use existing `aws_security_group.alb.id` from Spec 01 security_groups.tf
- [ ] 3.5 Add ALB tags with Name, Environment, and Project following naming convention
- [ ] 3.6 Add Target Group resource to `alb.tf` with name `petclinic-staging-tg-mumford`, target_type `ip` (required for Fargate), port 8080, protocol `HTTP`
- [ ] 3.7 Configure target group VPC: reference `aws_vpc.main.id` from vpc.tf
- [ ] 3.8 Configure target group health check: path `/actuator/health`, protocol `HTTP`, port 8080, interval 30 seconds, timeout 5 seconds, healthy_threshold 2, unhealthy_threshold 3, matcher `200`
- [ ] 3.9 Configure target group deregistration delay: 30 seconds for graceful shutdown
- [ ] 3.10 Add target group tags with Name and Environment
- [ ] 3.11 Add ALB Listener resource to `alb.tf` with load_balancer_arn from ALB resource, port 80, protocol `HTTP`
- [ ] 3.12 Configure listener default action: type `forward`, target_group_arn from target group resource
- [ ] 3.13 Add outputs to `terraform/staging/outputs.tf`: `alb_dns_name` (ALB DNS name), `alb_arn` (ALB ARN), `alb_target_group_arn` (target group ARN)
- [ ] 3.14 Run `terraform fmt` and `terraform validate` to ensure ALB configuration is valid
- [ ] 3.15 Run `terraform plan` to review ALB, target group, and listener changes
- [ ] 3.16 Run `terraform apply` to create ALB, target group, and HTTP listener
- [ ] 3.17 Wait for ALB provisioning (typically 2-3 minutes): check status becomes "active"
- [ ] 3.18 Verify ALB: `aws elbv2 describe-load-balancers --names petclinic-staging-alb-mumford --region us-east-1` and confirm State is "active"
- [ ] 3.19 Get ALB DNS name: `aws elbv2 describe-load-balancers --names petclinic-staging-alb-mumford --query 'LoadBalancers[0].DNSName' --output text`
- [ ] 3.20 Verify target group: `aws elbv2 describe-target-groups --names petclinic-staging-tg-mumford --region us-east-1` and confirm health check settings
- [ ] 3.21 Verify target group has no registered targets yet: `aws elbv2 describe-target-health --target-group-arn <arn> --region us-east-1` should show empty or no targets
- [ ] 3.22 Verify HTTP listener: `aws elbv2 describe-listeners --load-balancer-arn <arn> --region us-east-1` and confirm port 80 forwards to target group
- [ ] 3.23 Test ALB in browser: open `http://<alb-dns-name>` and verify it shows "503 Service Temporarily Unavailable" (expected, no targets registered yet)
- [ ] 3.24 Create proof artifacts: screenshot of browser showing 503 error, CLI outputs of ALB, target group, and listener descriptions
- [ ] 3.25 Commit ALB Terraform configuration with message: `feat: add Application Load Balancer with HTTP listener and target group`

---

### [x] 4.0 Deploy ECS Service with Auto-Scaling Policies

Launch ECS service with 2 desired tasks in private subnets, register with ALB target group, configure auto-scaling policies for CPU and memory utilization, and implement scheduled scaling for off-hours cost optimization.

#### 4.0 Proof Artifact(s)

- File: `terraform/staging/ecs.tf` updated with ECS service and auto-scaling resources demonstrates service configuration
- CLI output: `terraform apply` showing ECS service creation demonstrates provisioning
- CLI output: `aws ecs describe-services --cluster petclinic-staging-cluster-mumford --services petclinic-staging-service-mumford` demonstrates service exists and shows desired/running task counts
- CLI output: `aws ecs list-tasks --cluster petclinic-staging-cluster-mumford --service-name petclinic-staging-service-mumford` demonstrates tasks are running and shows task ARNs
- CLI output: `aws application-autoscaling describe-scaling-policies --service-namespace ecs` demonstrates auto-scaling policies exist for CPU and memory targets
- CLI output: `aws elbv2 describe-target-health --target-group-arn <arn>` demonstrates tasks are registered with ALB and shows health status (healthy/unhealthy)
- AWS Console screenshot: ECS service dashboard showing 2/2 running tasks demonstrates service is operational
- AWS Console screenshot: ALB target group showing 2 healthy targets demonstrates tasks are registered and passing health checks

#### 4.0 Tasks

- [ ] 4.1 Add variables to `terraform/staging/variables.tf`: `ecs_desired_count` (number, default 2), `ecs_min_count` (number, default 1), `ecs_max_count` (number, default 4)
- [ ] 4.2 Add variables for auto-scaling: `cpu_target_value` (number, default 70), `memory_target_value` (number, default 80)
- [ ] 4.3 Update `terraform/staging/ecs.tf` to add ECS Service resource with name `petclinic-staging-service-mumford`, cluster from ECS cluster, task_definition from task definition resource
- [ ] 4.4 Configure ECS service: desired_count from variable, launch_type `FARGATE`, platform_version `LATEST`
- [ ] 4.5 Configure ECS service deployment: deployment_maximum_percent 100, deployment_minimum_healthy_percent 0 (recreate strategy - stop all, then start new)
- [ ] 4.6 Configure ECS service network: awsvpc network configuration with subnets `[aws_subnet.private.id]` (private subnet), security_groups `[aws_security_group.app.id]` from Spec 01, assign_public_ip `true` (required for ECR access via NAT Gateway)
- [ ] 4.7 Configure ECS service load balancer: target_group_arn from ALB target group, container_name `petclinic`, container_port 8080
- [ ] 4.8 Configure ECS service health check grace period: 60 seconds to allow application startup before health checks
- [ ] 4.9 Add service depends_on: ALB listener to ensure ALB is ready before service creation
- [ ] 4.10 Add Application Auto Scaling Target resource: service_namespace `ecs`, resource_id `service/<cluster-name>/<service-name>`, scalable_dimension `ecs:service:DesiredCount`, min_capacity from variable, max_capacity from variable
- [ ] 4.11 Add Application Auto Scaling Policy for CPU: policy_type `TargetTrackingScaling`, target_tracking_scaling_policy_configuration with predefined_metric_type `ECSServiceAverageCPUUtilization`, target_value from variable (70)
- [ ] 4.12 Add Application Auto Scaling Policy for memory: policy_type `TargetTrackingScaling`, target_tracking_scaling_policy_configuration with predefined_metric_type `ECSServiceAverageMemoryUtilization`, target_value from variable (80)
- [ ] 4.13 Add Scheduled Scaling Action for off-hours (scale to 0): schedule `cron(0 22 ? * MON-FRI *)` (10 PM EST weekdays), min_capacity 0, max_capacity 0, scalable_target_action with min_capacity 0, max_capacity 0
- [ ] 4.14 Add Scheduled Scaling Action for business hours (scale to 2): schedule `cron(0 6 ? * MON-FRI *)` (6 AM EST weekdays), scalable_target_action with min_capacity 1, max_capacity 4, desired_capacity 2
- [ ] 4.15 Add outputs to `terraform/staging/outputs.tf`: `ecs_service_name` (service name), `ecs_cluster_name` (cluster name), `ecs_service_id` (service ID)
- [ ] 4.16 Run `terraform fmt` and `terraform validate` to ensure ECS service and auto-scaling configurations are valid
- [ ] 4.17 Run `terraform plan` to review ECS service, auto-scaling target, policies, and scheduled actions
- [ ] 4.18 Run `terraform apply` to create ECS service and auto-scaling configuration
- [ ] 4.19 Wait for service deployment (tasks starting): monitor with `aws ecs describe-services --cluster petclinic-staging-cluster-mumford --services petclinic-staging-service-mumford --region us-east-1` until runningCount equals desiredCount
- [ ] 4.20 Verify service status: `aws ecs describe-services --cluster petclinic-staging-cluster-mumford --services petclinic-staging-service-mumford --region us-east-1` shows desiredCount 2, runningCount 2, status ACTIVE
- [ ] 4.21 List running tasks: `aws ecs list-tasks --cluster petclinic-staging-cluster-mumford --service-name petclinic-staging-service-mumford --region us-east-1`
- [ ] 4.22 Describe tasks to verify status: `aws ecs describe-tasks --cluster petclinic-staging-cluster-mumford --tasks <task-arn> --region us-east-1` shows lastStatus RUNNING
- [ ] 4.23 Verify auto-scaling policies: `aws application-autoscaling describe-scaling-policies --service-namespace ecs --region us-east-1 | grep petclinic` shows CPU and memory target tracking policies
- [ ] 4.24 Verify scheduled scaling actions: `aws application-autoscaling describe-scheduled-actions --service-namespace ecs --region us-east-1` shows off-hours and business-hours schedules
- [ ] 4.25 Check target health: `aws elbv2 describe-target-health --target-group-arn <arn> --region us-east-1` should show 2 targets with TargetHealth.State "healthy" or "initial" (wait for health checks to pass)
- [ ] 4.26 Wait for targets to become healthy (may take 1-2 minutes for health checks): periodically check target health until both show "healthy"
- [ ] 4.27 Verify in AWS Console: Navigate to ECS > Clusters > petclinic-staging-cluster-mumford > Services and confirm 2/2 tasks running
- [ ] 4.28 Verify in AWS Console: Navigate to EC2 > Load Balancers > Target Groups > petclinic-staging-tg-mumford > Targets tab and confirm 2 healthy targets
- [ ] 4.29 Create proof artifacts: screenshots of ECS service dashboard (2/2 tasks), ALB target group (2 healthy targets), CLI outputs of service, tasks, auto-scaling policies, and target health
- [ ] 4.30 Commit ECS service and auto-scaling configuration with message: `feat: add ECS service with auto-scaling and scheduled scaling`

---

### [x] 5.0 Validate End-to-End Application Access and Monitoring

Verify Pet Clinic application is fully operational via ALB DNS, successfully connected to RDS PostgreSQL database, serving content correctly, passing health checks, and logging to CloudWatch with comprehensive proof artifacts.

#### 5.0 Proof Artifact(s)

- Browser screenshot: Pet Clinic homepage accessed via `http://<alb-dns-name>` demonstrates application is publicly accessible
- Browser screenshot: Owners page (`/owners`) showing database records demonstrates database connectivity and data retrieval
- Browser screenshot: Veterinarians page (`/vets`) showing veterinarian data demonstrates full application functionality across multiple pages
- Browser screenshot: Find Owners search functionality demonstrates interactive features work correctly
- CLI output: `curl http://<alb-dns-name>/actuator/health` returning `{"status":"UP"}` demonstrates health endpoint works and application is healthy
- CLI output: `aws logs tail /ecs/petclinic-staging-mumford --follow` showing application startup logs demonstrates CloudWatch logging is working
- CloudWatch Logs screenshot: Log stream showing "Started PetClinicApplication" message demonstrates successful application startup
- CloudWatch Logs screenshot: Log entry showing database connection success (e.g., HikariCP initialization) demonstrates database connectivity from application logs
- AWS Console screenshot: ECS service showing 2/2 tasks running with "RUNNING" status demonstrates service stability
- AWS Console screenshot: ALB target group showing all targets healthy with consecutive successful health checks demonstrates consistent availability
- CLI output: `aws ecs describe-tasks --cluster petclinic-staging-cluster-mumford --tasks <task-id>` showing lastStatus=RUNNING and healthStatus=HEALTHY demonstrates task health
- CLI output: `terraform output` showing ALB DNS name and other infrastructure outputs demonstrates infrastructure is queryable

#### 5.0 Tasks

- [ ] 5.1 Get ALB DNS name: `terraform output -raw alb_dns_name` or `aws elbv2 describe-load-balancers --names petclinic-staging-alb-mumford --query 'LoadBalancers[0].DNSName' --output text`
- [ ] 5.2 Open browser and navigate to Pet Clinic homepage: `http://<alb-dns-name>` and verify page loads with Liatrio branding
- [ ] 5.3 Take screenshot of homepage showing Pet Clinic logo, navigation menu (Home, Owners, Veterinarians, Find Owners), and Liatrio branding footer
- [ ] 5.4 Navigate to "Find Owners" page and search for existing owners (or click "Find Owners" to list all)
- [ ] 5.5 Navigate to "Owners" list page (`/owners?lastName=`) and verify owner records are displayed from RDS PostgreSQL database
- [ ] 5.6 Take screenshot of Owners page showing owner names, addresses, and pets, demonstrating database connectivity
- [ ] 5.7 Click on an owner to view owner details page and verify pet information is displayed
- [ ] 5.8 Navigate to "Veterinarians" page (`/vets.html`) and verify veterinarian data is displayed with specialties
- [ ] 5.9 Take screenshot of Veterinarians page showing vet names and specialties (e.g., radiology, surgery, dentistry), demonstrating full application functionality
- [ ] 5.10 Test health endpoint: `curl http://<alb-dns-name>/actuator/health` and verify response is `{"status":"UP"}` or similar JSON with "UP" status
- [ ] 5.11 Test health endpoint with verbose: `curl -v http://<alb-dns-name>/actuator/health` and verify HTTP 200 status code
- [ ] 5.12 Verify CloudWatch logs: `aws logs tail /ecs/petclinic-staging-mumford --follow --region us-east-1` to see live application logs
- [ ] 5.13 Search CloudWatch logs for application startup: `aws logs filter-log-events --log-group-name /ecs/petclinic-staging-mumford --filter-pattern "Started PetClinicApplication" --region us-east-1` and verify startup message exists
- [ ] 5.14 Search CloudWatch logs for database connection: `aws logs filter-log-events --log-group-name /ecs/petclinic-staging-mumford --filter-pattern "HikariPool" --region us-east-1` and verify HikariCP connection pool initialization
- [ ] 5.15 Take screenshot of CloudWatch Logs Console showing log stream with "Started PetClinicApplication" message
- [ ] 5.16 Take screenshot of CloudWatch Logs Console showing database connection success log entries (HikariCP initialization or PostgreSQL connection)
- [ ] 5.17 Verify ECS service stability: `aws ecs describe-services --cluster petclinic-staging-cluster-mumford --services petclinic-staging-service-mumford --region us-east-1` shows runningCount 2, desiredCount 2, no deployment issues
- [ ] 5.18 Verify all tasks are healthy: `aws ecs describe-tasks --cluster petclinic-staging-cluster-mumford --tasks <task-id-1> <task-id-2> --region us-east-1` shows lastStatus RUNNING and healthStatus HEALTHY for both tasks
- [ ] 5.19 Take screenshot of ECS Console showing service with 2/2 tasks running in RUNNING status
- [ ] 5.20 Verify target health consistently shows healthy: `aws elbv2 describe-target-health --target-group-arn <arn> --region us-east-1` shows both targets with State "healthy"
- [ ] 5.21 Take screenshot of ALB Target Group Console showing 2 healthy targets with consecutive successful health checks
- [ ] 5.22 Verify Terraform outputs are accessible: `terraform output` shows all infrastructure values (VPC, subnets, ALB DNS, ECS cluster, service names, etc.)
- [ ] 5.23 Test application functionality: create a new owner or add a pet to verify write operations work (optional but demonstrates full CRUD functionality)
- [ ] 5.24 Test application across multiple pages: navigate through Find Owners → Owner Details → Add Pet → Veterinarians to ensure all features work
- [ ] 5.25 Monitor application for 5-10 minutes to ensure stability (no task restarts, health checks remain healthy)
- [ ] 5.26 Create comprehensive proof artifacts file: `docs/specs/02-spec-application-deployment/02-proofs/02-task-5.0-proofs.md` documenting all validation results with screenshots and CLI outputs
- [ ] 5.27 Include in proof artifacts: browser screenshots (homepage, owners, vets), curl health check output, CloudWatch logs screenshots, ECS service screenshot, ALB target group screenshot, CLI outputs of task descriptions and target health
- [ ] 5.28 Document ALB DNS name for future reference: `echo "Application URL: http://<alb-dns-name>" >> docs/specs/02-spec-application-deployment/02-proofs/02-task-5.0-proofs.md`
- [ ] 5.29 Update task list to mark all tasks as complete
- [ ] 5.30 Commit proof artifacts with message: `docs: add comprehensive end-to-end validation proof artifacts`

---

## Implementation Notes

### Commit Strategy

Follow AGENTS.md guidelines:
- Commit frequently after completing logical sub-tasks
- Use conventional commit format: `feat:`, `fix:`, `docs:`, `chore:`
- Include co-author tag: `Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>`
- Keep PRs under 500 lines unless impossible to avoid
- **IMPORTANT:** Documentation changes (README.md) must be in a separate PR from infrastructure code

### Terraform Best Practices

- Run `terraform validate` before `terraform apply`
- Run `terraform fmt` to format code consistently
- Use `terraform plan` to review changes before applying
- Add descriptive comments to resource blocks
- Use variables for configurable values (CPU, memory, desired count, etc.)
- Follow naming convention: `petclinic-{environment}-{resource-type}-mumford`

### Docker Best Practices

- Use multi-stage builds to minimize image size
- Place `.dockerignore` to exclude unnecessary files
- Run containers as non-root user for security
- Test Docker image locally before pushing to ECR
- Verify application starts and connects to database in container environment

### AWS Resource Dependencies

- ECR repository must exist before task definition can reference image URI
- ECS cluster must exist before task definition and service creation
- Task definition must exist before service creation
- ALB and target group must exist before ECS service can register targets
- IAM roles must exist before task definition can reference them
- Secrets Manager secret (from Spec 01) must exist for task definition secrets configuration

### Testing and Validation

- After each parent task completion, verify proof artifacts are collected
- Test infrastructure incrementally (don't wait until the end)
- Use AWS Console to visually confirm resource creation and configuration
- Verify application logs in CloudWatch after deployment
- Test health checks and auto-scaling behavior under load (optional but recommended)

### Cost Management

- Monitor AWS costs during development using Cost Explorer
- Scheduled scaling to 0 tasks during off-hours (configure cron schedule as needed)
- NAT Gateway is the most expensive component (~$32/month), shared with other resources
- ECS Fargate tasks cost ~$0.04/hour for 0.5 vCPU, 1 GB configuration (~$60/month for 2 tasks running 24/7)
- Cost optimization via scheduled scaling can reduce compute costs by ~50% for staging

### Secrets and Security

- Never commit database credentials or API keys to version control
- Use Secrets Manager ARNs in task definition, not plaintext values
- Redact sensitive information from proof artifacts (passwords, AWS account IDs)
- ECR authentication required: `aws ecr get-login-password | docker login --username AWS --password-stdin <ecr-uri>`
- Verify IAM roles follow least privilege principle

---

## Next Steps

Once all tasks are complete:
1. Verify all proof artifacts are collected in `docs/specs/02-spec-application-deployment/02-proofs/`
2. Run `/SDD-4-validate-spec-implementation` to validate implementation against spec
3. Prepare for Spec 03: CI/CD Pipeline (GitHub workflows for automated deployment and database migrations)

---

**Status:** Ready for implementation. All parent tasks and sub-tasks defined.
