# 01-tasks-aws-core-infrastructure.md

## Overview

This task list implements **Spec 01: AWS Core Infrastructure (Terraform)** for the Emerald Grove Pet Clinic application. The implementation establishes foundational AWS infrastructure including networking, database, security, and state management for both staging and production environments in us-east-1.

**Spec Reference:** `01-spec-aws-core-infrastructure.md`

**Implementation Strategy:** Environment-based directory structure with separate Terraform configurations for staging and production environments.

---

## Relevant Files

### Terraform Configuration Files (Staging Environment)

- `terraform/staging/backend.tf` - S3 backend configuration for Terraform state storage
- `terraform/staging/versions.tf` - Terraform and AWS provider version constraints
- `terraform/staging/provider.tf` - AWS provider configuration with region settings
- `terraform/staging/variables.tf` - Input variable definitions with descriptions and defaults
- `terraform/staging/terraform.tfvars` - Staging-specific variable values
- `terraform/staging/vpc.tf` - VPC, subnets, Internet Gateway, NAT Gateway, route tables
- `terraform/staging/security_groups.tf` - Security group definitions for ALB, application, and RDS
- `terraform/staging/rds.tf` - RDS PostgreSQL instance and DB subnet group
- `terraform/staging/secrets.tf` - AWS Secrets Manager resources for database credentials
- `terraform/staging/outputs.tf` - Output values for VPC, subnets, RDS, security groups, secrets
- `terraform/staging/.gitignore` - Ignore state files, .terraform directory, and sensitive files

### Documentation

- `terraform/README.md` - Comprehensive setup, deployment, and troubleshooting documentation

### Production Environment (Future)

- `terraform/production/*` - Same file structure as staging with production-specific values
- **Note:** Production environment will be created after staging is validated

### Notes

- Use Terraform >= 1.5.0 and AWS provider ~> 5.0
- All Terraform files should use standard formatting (run `terraform fmt` before committing)
- Sensitive files (terraform.tfstate, terraform.tfvars with secrets, .terraform/) are git-ignored
- Follow conventional commit format with co-author tag: `Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>`
- Documentation changes (README.md) should be in a separate PR per AGENTS.md

---

## Tasks

### [x] 1.0 Bootstrap Terraform State Backend Infrastructure

Bootstrap the S3 and DynamoDB resources required for Terraform remote state management, enabling team collaboration and preventing state conflicts.

#### 1.0 Proof Artifact(s)

- Documentation: `terraform/README.md` section documenting S3 bucket and DynamoDB table creation commands demonstrates bootstrap instructions
- CLI output: `aws s3 ls s3://petclinic-terraform-state-mumford` showing bucket exists demonstrates S3 backend creation
- CLI output: `aws dynamodb describe-table --table-name petclinic-terraform-locks-mumford --region us-east-1` showing table with LockID key demonstrates DynamoDB lock table creation
- Terraform configuration: `terraform/staging/backend.tf` with S3 backend configuration demonstrates state management setup
- CLI output: `terraform init` output from staging directory showing "Successfully configured the backend" demonstrates working state backend
- **Proof file:** `docs/specs/01-spec-aws-deployment/01-proofs/01-task-1.0-proofs.md`

#### 1.0 Tasks

- [x] 1.1 Create S3 bucket manually using AWS CLI with name `petclinic-terraform-state-mumford`, enable versioning and encryption (AES256)
- [x] 1.2 Create DynamoDB table manually using AWS CLI with name `petclinic-terraform-locks-mumford`, partition key `LockID` (String), billing mode PAY_PER_REQUEST
- [x] 1.3 Create `terraform/staging/` directory structure
- [x] 1.4 Create `terraform/staging/backend.tf` with S3 backend configuration referencing bucket and DynamoDB table
- [x] 1.5 Create `terraform/staging/.gitignore` to exclude `*.tfstate`, `*.tfstate.*`, `.terraform/`, `.terraform.lock.hcl`, and `*.tfvars` (for sensitive values)
- [x] 1.6 Run `terraform init` from `terraform/staging/` to initialize backend and verify connection
- [x] 1.7 Document bootstrap process in `terraform/README.md` with AWS CLI commands for S3 bucket and DynamoDB table creation
- [x] 1.8 Commit state backend configuration with message: `feat: add Terraform state backend configuration for staging`

---

### [ ] 2.0 Create VPC and Network Infrastructure

Provision VPC with public and private subnets, Internet Gateway, NAT Gateway, and route tables for both staging and production environments.

#### 2.0 Proof Artifact(s)

- CLI output: `terraform apply` output showing VPC, subnets, IGW, NAT Gateway, and route tables created demonstrates infrastructure provisioning
- CLI output: `terraform state list` showing all networking resources (aws_vpc, aws_subnet, aws_internet_gateway, aws_nat_gateway, aws_route_table) demonstrates infrastructure tracking
- CLI output: `terraform output` showing VPC ID and subnet IDs demonstrates output configuration
- AWS Console screenshot: VPC dashboard showing VPC with CIDR 10.0.0.0/16, one public subnet (10.0.1.0/24), one private subnet (10.0.2.0/24) demonstrates network creation
- AWS Console screenshot: Route tables showing public subnet associated with IGW and private subnet associated with NAT Gateway demonstrates proper routing configuration

#### 2.0 Tasks

- [ ] 2.1 Create `terraform/staging/versions.tf` with Terraform >= 1.5.0 and AWS provider ~> 5.0 version constraints
- [ ] 2.2 Create `terraform/staging/provider.tf` with AWS provider configuration for us-east-1 region
- [ ] 2.3 Create `terraform/staging/variables.tf` with variables: environment (string), vpc_cidr (string, default "10.0.0.0/16"), public_subnet_cidr (string, default "10.0.1.0/24"), private_subnet_cidr (string, default "10.0.2.0/24"), az (string, default "us-east-1a")
- [ ] 2.4 Create `terraform/staging/terraform.tfvars` with `environment = "staging"`
- [ ] 2.5 Create `terraform/staging/vpc.tf` with VPC resource (CIDR 10.0.0.0/16) and tags including environment and Name = "petclinic-staging-vpc"
- [ ] 2.6 Add public subnet (10.0.1.0/24) to `vpc.tf` with `map_public_ip_on_launch = true` and Name tag "petclinic-staging-public-subnet"
- [ ] 2.7 Add private subnet (10.0.2.0/24) to `vpc.tf` with Name tag "petclinic-staging-private-subnet"
- [ ] 2.8 Add Internet Gateway to `vpc.tf` attached to VPC with Name tag "petclinic-staging-igw"
- [ ] 2.9 Add Elastic IP for NAT Gateway to `vpc.tf` with tags
- [ ] 2.10 Add NAT Gateway in public subnet to `vpc.tf` with Elastic IP association and Name tag "petclinic-staging-nat"
- [ ] 2.11 Add public route table to `vpc.tf` with route to 0.0.0.0/0 via Internet Gateway and Name tag "petclinic-staging-public-rt"
- [ ] 2.12 Add private route table to `vpc.tf` with route to 0.0.0.0/0 via NAT Gateway and Name tag "petclinic-staging-private-rt"
- [ ] 2.13 Add route table associations to `vpc.tf` linking public subnet to public route table and private subnet to private route table
- [ ] 2.14 Run `terraform validate` and `terraform fmt` to verify and format configuration
- [ ] 2.15 Run `terraform plan` to review planned infrastructure changes
- [ ] 2.16 Run `terraform apply` to create VPC and networking infrastructure (approve when prompted)
- [ ] 2.17 Verify VPC, subnets, IGW, NAT Gateway, and route tables in AWS Console
- [ ] 2.18 Commit networking infrastructure with message: `feat: add VPC and network infrastructure for staging`

---

### [ ] 3.0 Configure Security Groups for Database and Application

Create security groups that restrict database access to application resources only and prepare for future load balancer integration.

#### 3.0 Proof Artifact(s)

- CLI output: `terraform apply` output showing security groups created demonstrates security configuration
- CLI output: `terraform state list | grep aws_security_group` showing all security group resources demonstrates security group tracking
- AWS Console screenshot: Security groups dashboard showing `petclinic-staging-rds-sg` with ingress rule for PostgreSQL (port 5432) from application security group demonstrates database security
- AWS Console screenshot: Security groups dashboard showing `petclinic-staging-app-sg` with ingress rules for port 8080 from ALB security group demonstrates application access control
- AWS Console screenshot: Security groups dashboard showing `petclinic-staging-alb-sg` with ingress rules for HTTP (80) and HTTPS (443) from 0.0.0.0/0 demonstrates load balancer public access

#### 3.0 Tasks

- [ ] 3.1 Create `terraform/staging/security_groups.tf` file
- [ ] 3.2 Add ALB security group resource to `security_groups.tf` with name "petclinic-staging-alb-sg", description "Security group for Application Load Balancer"
- [ ] 3.3 Add ingress rules to ALB security group for HTTP (port 80) and HTTPS (port 443) from 0.0.0.0/0
- [ ] 3.4 Add application security group resource to `security_groups.tf` with name "petclinic-staging-app-sg", description "Security group for application instances"
- [ ] 3.5 Add ingress rule to application security group for port 8080 from ALB security group (use security_group_id reference)
- [ ] 3.6 Add RDS security group resource to `security_groups.tf` with name "petclinic-staging-rds-sg", description "Security group for RDS PostgreSQL database"
- [ ] 3.7 Add ingress rule to RDS security group for PostgreSQL (port 5432) from application security group (use security_group_id reference)
- [ ] 3.8 Add egress rules to all three security groups allowing all outbound traffic (0.0.0.0/0, all protocols)
- [ ] 3.9 Add tags to all security groups including environment and descriptive Name tags
- [ ] 3.10 Run `terraform validate` and `terraform fmt` to verify and format configuration
- [ ] 3.11 Run `terraform plan` to review security group changes
- [ ] 3.12 Run `terraform apply` to create security groups
- [ ] 3.13 Verify security groups and rules in AWS Console EC2 Security Groups dashboard
- [ ] 3.14 Commit security group configuration with message: `feat: add security groups for ALB, application, and RDS`

---

### [ ] 4.0 Deploy RDS PostgreSQL Database with Secrets Manager

Provision RDS PostgreSQL database instance in private subnet with automated backups, encryption, and AWS Secrets Manager integration for credential management.

#### 4.0 Proof Artifact(s)

- CLI output: `terraform apply` output showing RDS instance and Secrets Manager secret created demonstrates database provisioning
- AWS Console screenshot: RDS dashboard showing `petclinic-staging-db` database instance in "Available" status demonstrates successful deployment
- AWS Console screenshot: Secrets Manager showing secret `petclinic/staging/database` with last rotation status demonstrates credential storage
- CLI output: `aws secretsmanager get-secret-value --secret-id petclinic/staging/database --region us-east-1 --query SecretString` (with password redacted in documentation) demonstrates secret retrieval
- CLI output: Connection test using `psql` with credentials from Secrets Manager showing successful connection and `\l` listing petclinic database demonstrates database accessibility

#### 4.0 Tasks

- [ ] 4.1 Create `terraform/staging/rds.tf` file
- [ ] 4.2 Add `random_password` resource to `rds.tf` for database master password (length 16, special characters allowed, no override_special to avoid Terraform special chars)
- [ ] 4.3 Add DB subnet group resource to `rds.tf` with name "petclinic-staging-db-subnet-group" including the private subnet
- [ ] 4.4 Add RDS PostgreSQL instance resource to `rds.tf` with identifier "petclinic-staging-db", engine "postgres", engine_version "16.3" (or latest 16.x)
- [ ] 4.5 Configure RDS instance with: instance_class "db.t3.micro", allocated_storage 20, storage_type "gp3", storage_encrypted true, db_name "petclinic", username "petclinic", password from random_password resource
- [ ] 4.6 Configure RDS with: db_subnet_group_name, vpc_security_group_ids (RDS security group), publicly_accessible false, skip_final_snapshot true (for staging), backup_retention_period 1 (staging), multi_az false
- [ ] 4.7 Add variable `backup_retention_days` to `variables.tf` with default 1, and use in RDS backup_retention_period
- [ ] 4.8 Create `terraform/staging/secrets.tf` file
- [ ] 4.9 Add Secrets Manager secret resource to `secrets.tf` with name "petclinic/staging/database", description "Database credentials for Pet Clinic staging"
- [ ] 4.10 Add Secrets Manager secret version resource to `secrets.tf` with JSON string containing: host (RDS endpoint), port "5432", username "petclinic", password (from random_password), dbname "petclinic", engine "postgres"
- [ ] 4.11 Run `terraform validate` and `terraform fmt` to verify and format configuration
- [ ] 4.12 Run `terraform plan` to review RDS and Secrets Manager changes
- [ ] 4.13 Run `terraform apply` to create RDS instance and secrets (note: RDS creation takes 10-15 minutes, be patient)
- [ ] 4.14 Verify RDS instance status is "Available" in AWS Console RDS dashboard
- [ ] 4.15 Verify secret exists in AWS Console Secrets Manager with correct name "petclinic/staging/database"
- [ ] 4.16 Retrieve database credentials using `aws secretsmanager get-secret-value --secret-id petclinic/staging/database --region us-east-1 --query SecretString --output text | jq .`
- [ ] 4.17 Test database connectivity using `psql` with credentials from Secrets Manager (install PostgreSQL client if needed), run `\l` to list databases and verify "petclinic" exists
- [ ] 4.18 Commit RDS and Secrets Manager configuration with message: `feat: add RDS PostgreSQL database and Secrets Manager integration`

---

### [ ] 5.0 Configure Terraform Outputs and Complete Documentation

Define Terraform outputs for all infrastructure resources and create comprehensive documentation for deployment, troubleshooting, and infrastructure management.

#### 5.0 Proof Artifact(s)

- CLI output: `terraform output` showing VPC ID, subnet IDs, security group IDs, RDS endpoint, and Secrets Manager ARN demonstrates output configuration
- Documentation: `terraform/README.md` with sections for prerequisites, state backend bootstrap, staging deployment, production deployment, credential retrieval, and troubleshooting demonstrates documentation completeness
- Terraform file: `terraform/staging/variables.tf` with descriptions and default values for all variables demonstrates configuration management
- AWS Console screenshot: Resource tagging showing consistent naming pattern (petclinic-staging-vpc, petclinic-staging-rds, petclinic-staging-app-sg) demonstrates naming conventions
- CLI output: `terraform fmt -check -recursive terraform/` showing "no changes" demonstrates code formatting compliance

#### 5.0 Tasks

- [ ] 5.1 Create `terraform/staging/outputs.tf` file
- [ ] 5.2 Add outputs to `outputs.tf` for: vpc_id, public_subnet_id, private_subnet_id (with descriptions)
- [ ] 5.3 Add outputs for: alb_security_group_id, app_security_group_id, rds_security_group_id (with descriptions)
- [ ] 5.4 Add outputs for: rds_endpoint, rds_port, rds_database_name (with descriptions)
- [ ] 5.5 Add outputs for: secrets_manager_secret_arn, secrets_manager_secret_name (with descriptions)
- [ ] 5.6 Run `terraform apply` (should show no changes, just register outputs)
- [ ] 5.7 Run `terraform output` to verify all outputs display correctly with values
- [ ] 5.8 Update `terraform/README.md` with Prerequisites section: Terraform >= 1.5.0, AWS CLI configured, appropriate IAM permissions
- [ ] 5.9 Add State Backend Bootstrap section to README with AWS CLI commands for S3 bucket and DynamoDB table creation
- [ ] 5.10 Add Staging Deployment section to README with: `cd terraform/staging`, `terraform init`, `terraform plan`, `terraform apply` steps
- [ ] 5.11 Add Production Deployment section to README (placeholder noting production setup follows same pattern as staging with different directory)
- [ ] 5.12 Add Retrieving Database Credentials section to README with AWS CLI command for Secrets Manager retrieval and example of parsing JSON output
- [ ] 5.13 Add Troubleshooting section to README with common issues: state lock errors, RDS creation timeout, connectivity issues, and resolutions
- [ ] 5.14 Add Terraform Outputs section to README listing all available outputs and their purposes
- [ ] 5.15 Verify all variables in `variables.tf` have descriptions and appropriate defaults
- [ ] 5.16 Run `terraform fmt -check -recursive terraform/` to verify all files are properly formatted
- [ ] 5.17 Verify naming conventions in AWS Console: all resources should follow pattern "petclinic-{environment}-{resource-type}"
- [ ] 5.18 Take screenshots of AWS Console showing: VPC dashboard, Security Groups, RDS instance, Secrets Manager (for proof artifacts)
- [ ] 5.19 Commit outputs configuration with message: `feat: add Terraform outputs for all infrastructure resources`
- [ ] 5.20 Commit README.md documentation in **separate PR** with message: `docs: add comprehensive Terraform deployment documentation`

---

## Implementation Notes

### Commit Strategy

Follow AGENTS.md guidelines:
- Commit frequently after completing logical sub-tasks
- Use conventional commit format: `feat:`, `fix:`, `docs:`, `chore:`
- Include co-author tag: `Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>`
- Keep PRs under 500 lines unless impossible to avoid
- **IMPORTANT:** Documentation changes (README.md) must be in a separate PR from infrastructure code

### Testing and Validation

Before each `terraform apply`:
- Run `terraform validate` to check configuration syntax
- Run `terraform fmt` to format code consistently
- Run `terraform plan` to review changes before applying
- Review the plan output carefully for unexpected changes

### Cost Management

- NAT Gateway: ~$32/month + data transfer costs
- RDS db.t3.micro: ~$15-20/month
- Secrets Manager: $0.40/month per secret
- S3/DynamoDB state storage: Negligible (~$0.10/month)
- **Total estimated cost per environment: ~$50-60/month**

### Production Environment

After staging is validated and working:
1. Copy `terraform/staging/` directory to `terraform/production/`
2. Update `terraform.tfvars` with `environment = "production"`
3. Update `backup_retention_days` to 7 in production tfvars
4. Set `skip_final_snapshot = false` in production RDS configuration
5. Consider enabling `multi_az = true` for production RDS when scaling up
6. Use separate state backend key in `backend.tf` or separate S3 prefix

### Proof Artifacts Collection

As you complete tasks, collect proof artifacts:
- Save CLI outputs to text files in `docs/specs/01-spec-aws-deployment/proof-artifacts/`
- Take screenshots of AWS Console and save with descriptive names
- Redact sensitive information (passwords, AWS account IDs) before committing
- Reference proof artifacts in PR descriptions

---

## Next Steps

Once all tasks are complete:
1. Verify all proof artifacts are collected
2. Run `/SDD-4-validate-spec-implementation` to validate implementation against spec
3. Prepare for Spec 02: Application Deployment (ECS/EKS setup)

---

**Status:** Ready for implementation. Start with Task 1.0 and proceed sequentially.
