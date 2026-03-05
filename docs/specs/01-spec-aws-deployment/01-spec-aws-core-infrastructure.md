# 01-spec-aws-core-infrastructure.md

## Introduction/Overview

This specification defines the foundational AWS infrastructure for the Emerald Grove Pet Clinic application using Terraform as Infrastructure as Code (IaC). The infrastructure will support two environments (staging and production) deployed in AWS us-east-1 region, with a focus on cost optimization while maintaining production-ready architecture. This spec establishes networking, database, security, and state management foundations that subsequent specs will build upon.

## Goals

- Create reproducible AWS infrastructure using Terraform for staging and production environments
- Establish secure networking with VPC, public/private subnets, and proper security group configurations
- Deploy Amazon RDS PostgreSQL database with AWS Secrets Manager for credential management
- Implement Terraform state management using S3 backend with DynamoDB locking for team collaboration
- Enable cost-effective infrastructure that can scale up as application requirements grow

## User Stories

**As a DevOps engineer**, I want infrastructure defined as code using Terraform so that I can version control, review, and reproduce AWS environments consistently.

**As a platform engineer**, I want separate staging and production environments so that I can test infrastructure changes safely before applying them to production.

**As a security engineer**, I want database credentials stored in AWS Secrets Manager so that sensitive information is never committed to version control or exposed in plain text.

**As a development team member**, I want the database schema to initialize automatically on application startup so that I don't need to manually run SQL scripts for each environment.

**As a cost-conscious stakeholder**, I want infrastructure that starts minimal (single AZ) so that we optimize costs while maintaining the ability to scale to multi-AZ when needed.

## Demoable Units of Work

### Unit 1: Terraform State Backend Setup

**Purpose:** Establish centralized Terraform state management to enable team collaboration and prevent state conflicts when multiple engineers work on infrastructure.

**Functional Requirements:**
- The system shall provide clear instructions for creating an S3 bucket for Terraform state storage in us-east-1
- The system shall provide clear instructions for creating a DynamoDB table for Terraform state locking in us-east-1
- The system shall include Terraform backend configuration that references the S3 bucket and DynamoDB table
- The system shall include a README or setup guide documenting the one-time bootstrap process
- The infrastructure shall use separate state files for staging and production environments

**Proof Artifacts:**
- Documentation file: `terraform/README.md` demonstrates bootstrap instructions exist
- Terraform configuration: Backend configuration in `terraform/staging/backend.tf` and `terraform/production/backend.tf` demonstrates state management setup
- CLI output: `terraform init` output showing successful S3 backend initialization demonstrates working state backend

### Unit 2: VPC and Network Infrastructure

**Purpose:** Create isolated network infrastructure with public and private subnets to securely host application and database resources while enabling internet access through a load balancer.

**Functional Requirements:**
- The system shall create a VPC in us-east-1 with CIDR block 10.0.0.0/16
- The system shall create one public subnet (10.0.1.0/24) for internet-facing resources (load balancers, NAT gateway)
- The system shall create one private subnet (10.0.2.0/24) for application and database resources
- The system shall create an Internet Gateway attached to the VPC for public subnet internet access
- The system shall create a NAT Gateway in the public subnet for private subnet outbound internet access
- The system shall create appropriate route tables associating subnets with Internet Gateway or NAT Gateway
- The system shall tag all resources with environment name (staging or production) and project identifier
- The infrastructure shall be deployable to both staging and production with environment-specific naming

**Proof Artifacts:**
- Terraform output: `terraform apply` output showing VPC, subnets, IGW, NAT Gateway, and route tables created demonstrates infrastructure provisioning
- AWS Console screenshot: VPC dashboard showing VPC with CIDR 10.0.0.0/16 and associated subnets demonstrates network creation
- AWS Console screenshot: Route tables showing correct associations demonstrates proper routing configuration
- Terraform state: `terraform state list` showing all networking resources demonstrates infrastructure tracking

### Unit 3: Security Groups and Network Security

**Purpose:** Define network access controls that restrict database access to application resources only while preparing for future application load balancer integration.

**Functional Requirements:**
- The system shall create a security group for RDS database allowing PostgreSQL (port 5432) ingress only from application security group
- The system shall create a security group for application resources (to be used in Spec 02) allowing HTTP (port 80) and HTTPS (port 443) ingress
- The system shall create a security group for application resources allowing ingress from load balancer security group on port 8080 (Spring Boot default)
- All security groups shall have egress rules allowing all outbound traffic (0.0.0.0/0)
- The system shall tag security groups with environment and purpose identifiers
- Security group rules shall follow principle of least privilege (no 0.0.0.0/0 ingress except for ALB)

**Proof Artifacts:**
- Terraform output: `terraform apply` output showing security groups and rules created demonstrates security configuration
- AWS Console screenshot: Security groups dashboard showing RDS security group with port 5432 restricted to application SG demonstrates database security
- AWS Console screenshot: Application security group showing port 8080 access from load balancer demonstrates application access control
- Terraform configuration: Security group definitions in code demonstrates infrastructure as code implementation

### Unit 4: RDS PostgreSQL Database

**Purpose:** Deploy a managed PostgreSQL database with automated backups, security group restrictions, and AWS Secrets Manager integration for credential management.

**Functional Requirements:**
- The system shall create an RDS PostgreSQL database instance (version 16.x or latest stable) in the private subnet
- The system shall use db.t3.micro instance class for cost optimization (upgradable via variables)
- The system shall configure the database with 20GB gp3 storage (expandable)
- The system shall deploy the database in a single availability zone initially (configurable for multi-AZ)
- The system shall enable automated backups with 7-day retention for production, 1-day for staging
- The system shall generate a random database password using Terraform random provider
- The system shall store database credentials (endpoint, username, password, database name) in AWS Secrets Manager
- The system shall create a secret per environment with naming convention: `petclinic/{environment}/database`
- The system shall configure database with security group allowing access only from application security group
- The system shall set database name to "petclinic"
- The system shall set master username to "petclinic"
- The system shall enable encryption at rest for the database
- The system shall configure publicly_accessible = false for security
- The system shall skip final snapshot for staging environment, require final snapshot for production

**Proof Artifacts:**
- Terraform output: `terraform apply` output showing RDS instance created with endpoint demonstrates database provisioning
- AWS Console screenshot: RDS dashboard showing petclinic database instance in "Available" status demonstrates successful deployment
- AWS Console screenshot: Secrets Manager showing secret `petclinic/staging/database` with database credentials demonstrates credential storage
- CLI output: `aws secretsmanager get-secret-value --secret-id petclinic/staging/database --region us-east-1` (credentials redacted) demonstrates secret retrieval
- Database connectivity test: CLI output from `psql` or database client connecting to RDS endpoint using credentials from Secrets Manager demonstrates database accessibility

### Unit 5: Terraform Outputs and Documentation

**Purpose:** Provide clear outputs from Terraform and documentation to enable application deployment teams to consume infrastructure details and credentials.

**Functional Requirements:**
- The system shall output VPC ID, subnet IDs, and security group IDs for use in subsequent deployment specs
- The system shall output RDS endpoint and port for application configuration
- The system shall output AWS Secrets Manager secret ARN and name for application credential retrieval
- The system shall include a README.md in terraform/ directory documenting:
  - Prerequisites (Terraform version, AWS CLI, credentials setup)
  - State backend bootstrap instructions
  - How to deploy staging environment
  - How to deploy production environment
  - How to retrieve database credentials from Secrets Manager
  - Common troubleshooting scenarios
- The system shall include variable definitions with descriptions and default values
- The system shall use consistent naming conventions across all resources (petclinic-{environment}-{resource-type})

**Proof Artifacts:**
- Terraform output: `terraform output` command showing all defined outputs (VPC, subnets, RDS endpoint, secrets ARN) demonstrates output configuration
- Documentation file: `terraform/README.md` with complete setup and usage instructions demonstrates documentation completeness
- Terraform files: Variables file with descriptions and defaults demonstrates configuration management
- AWS Console screenshot: All resources showing consistent naming pattern (petclinic-staging-vpc, petclinic-staging-rds, etc.) demonstrates naming conventions

## Non-Goals (Out of Scope)

1. **Application deployment infrastructure** - ECS/EKS clusters, container repositories, and application-specific IAM roles are covered in Spec 02
2. **CI/CD pipeline configuration** - GitHub Actions workflows, deployment automation, and pipeline definitions are covered in Spec 03
3. **Monitoring and observability** - CloudWatch dashboards, alarms, log aggregation, and metrics are covered in Spec 04
4. **Multi-region deployment** - All infrastructure will be deployed to us-east-1 only; multi-region is out of scope
5. **Database migration or seeding** - Application handles schema creation on first boot; no Terraform-managed database initialization
6. **Custom domain and SSL certificates** - Route53 DNS and ACM certificates for custom domains are deferred to future work
7. **VPN or bastion host** - Direct database access for debugging is out of scope; application-only access initially
8. **WAF (Web Application Firewall)** - Advanced security controls are out of scope for initial deployment
9. **Auto-scaling configuration** - Application scaling policies will be defined in Spec 02 with ECS/EKS setup
10. **Cost budgets and alerts** - AWS Cost Explorer setup and budget alerts are out of scope

## Design Considerations

No specific design requirements identified. This spec focuses on infrastructure provisioning without UI/UX components.

## Repository Standards

Implementation must follow patterns established in the repository:

**From CLAUDE.md:**
- Strict Test-Driven Development (TDD) - While Terraform itself doesn't have unit tests in traditional sense, use `terraform validate`, `terraform plan`, and `terraform fmt` as validation steps
- Conventional commits: Use `feat:`, `fix:`, `docs:`, `chore:` prefixes
- Include co-author tag: `Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>`
- Google Java Style does not apply to Terraform; use Terraform standard formatting (terraform fmt)

**From AGENTS.md:**
- Commit code frequently to working branch
- Keep PRs under 500 lines unless impossible to avoid (Terraform configs may approach this limit)
- Ensure PRs are human-readable with clear descriptions
- Documentation changes (terraform/README.md) should be in separate PR from infrastructure code
- Production-level code quality standards apply to Terraform configurations

**Terraform Best Practices:**
- Use variables for all environment-specific values
- Use consistent naming conventions with environment prefix
- Tag all resources with environment, project, and managed-by tags
- Use data sources for AMI lookups or existing resource references
- Separate concerns: networking, database, security in separate .tf files
- Use terraform workspaces or separate directories for environment isolation (per user preference: separate directories)
- Pin provider versions in versions.tf
- Use remote state backend for team collaboration

## Technical Considerations

**Terraform Version:**
- Use Terraform >= 1.5.0 for best stability and features
- Pin AWS provider to ~> 5.0 for consistency

**AWS Provider Configuration:**
- Use AWS CLI profile or environment variables for authentication
- Do not hardcode AWS credentials in Terraform configurations
- Region must be explicitly set to us-east-1 in provider configuration

**State Management:**
- S3 bucket must have versioning enabled for state history
- S3 bucket must have encryption at rest enabled (AES256 or KMS)
- DynamoDB table must use LockID as partition key for state locking
- Use separate state files for staging and production (via separate directories)

**Database Configuration:**
- RDS PostgreSQL version should be 16.x or latest stable version available
- Use parameter groups with sensible defaults (Terraform aws_db_parameter_group)
- Database engine version should be explicitly specified to prevent unexpected upgrades
- Use subnet groups spanning the private subnet for RDS placement

**Networking:**
- VPC CIDR 10.0.0.0/16 provides 65,536 IPs (more than sufficient)
- Public subnet: 10.0.1.0/24 (256 IPs for load balancers, NAT gateway)
- Private subnet: 10.0.2.0/24 (256 IPs for application, RDS)
- Future expansion: Reserve 10.0.3.0/24 and 10.0.4.0/24 for additional subnets if multi-AZ upgrade needed
- NAT Gateway incurs hourly charges (~$0.045/hour) + data transfer costs

**Secrets Manager:**
- Secret rotation is out of scope initially but can be enabled later
- Secret values should use JSON format with keys: host, port, username, password, dbname
- Secrets should have environment-specific naming for isolation

**Cost Considerations:**
- db.t3.micro: ~$15-20/month per environment
- NAT Gateway: ~$32/month + data transfer per environment
- S3 state storage: Negligible (~$0.10/month)
- Total estimated cost per environment: ~$50-60/month
- Secrets Manager: $0.40/month per secret

**Dependencies:**
- Requires AWS CLI installed and configured with appropriate credentials
- Requires Terraform binary installed locally (>= 1.5.0)
- Requires appropriate IAM permissions for creating VPC, RDS, Secrets Manager, Security Groups

## Security Considerations

**Sensitive Data Handling:**
- Database passwords are generated by Terraform random provider and stored in Secrets Manager
- Terraform state will contain sensitive data (database passwords) - S3 bucket must have encryption and restricted access
- AWS Secrets Manager secrets must use encryption at rest (default AWS managed key acceptable)
- Never commit AWS credentials or database passwords to version control
- Use .gitignore to exclude terraform.tfstate, terraform.tfvars (if used for sensitive values), and .terraform/ directory

**Proof Artifact Security:**
- When sharing proof artifacts (CLI outputs, screenshots), redact database passwords and AWS account IDs
- Do not commit screenshots containing sensitive information to repository
- Store proof artifacts in separate secure location or redact before committing

**Network Security:**
- RDS database must never be publicly accessible (publicly_accessible = false)
- Security groups must follow least privilege principle
- No SSH access or bastion host in initial spec (administrative access out of scope)

**IAM and Access Control:**
- Terraform execution requires IAM permissions: ec2:*, rds:*, secretsmanager:*, s3:*, dynamodb:* for state backend
- Consider using separate IAM user or role for Terraform with restricted permissions
- Secrets Manager secrets should use resource-based policies limiting access to application IAM roles (defined in Spec 02)

**Compliance:**
- Encryption at rest enabled for RDS (meets basic compliance requirements)
- S3 bucket versioning and encryption enabled for state backend (audit trail)
- No specific compliance frameworks (HIPAA, SOC2) required at this stage

## Success Metrics

1. **Infrastructure Reproducibility** - Both staging and production environments can be created from scratch using `terraform apply` in under 15 minutes
2. **State Management** - Multiple team members can run Terraform commands concurrently without state conflicts (verified by DynamoDB lock acquisition)
3. **Database Connectivity** - Application can successfully connect to RDS PostgreSQL using credentials retrieved from AWS Secrets Manager (tested via psql or application connection)
4. **Cost Efficiency** - Monthly AWS bill per environment stays under $60 (verified after first full billing cycle)
5. **Code Quality** - All Terraform configurations pass `terraform validate` and `terraform fmt -check` without errors

## Open Questions

No open questions at this time. All requirements have been clarified through two rounds of questions.
