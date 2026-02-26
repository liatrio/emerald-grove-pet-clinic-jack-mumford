# Terraform Infrastructure for Emerald Grove Pet Clinic

This directory contains Terraform Infrastructure as Code (IaC) for deploying the Pet Clinic application to AWS.

## Architecture Overview

- **Region**: us-east-1 (N. Virginia)
- **Environments**: Staging and Production (separate directories)
- **State Management**: S3 backend with DynamoDB locking
- **Naming Convention**: `petclinic-{environment}-{resource-type}-mumford`

## Prerequisites

Before using these Terraform configurations, ensure you have:

- **Terraform** >= 1.5.0 installed ([Download](https://www.terraform.io/downloads))
- **AWS CLI** configured with valid credentials ([Setup Guide](https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-configure.html))
- **Appropriate IAM Permissions** for creating:
  - VPC, Subnets, Internet Gateway, NAT Gateway
  - Security Groups
  - RDS PostgreSQL instances
  - AWS Secrets Manager secrets
  - S3 buckets (for state backend)
  - DynamoDB tables (for state locking)

### Verify Prerequisites

```bash
# Check Terraform version
terraform version

# Check AWS CLI configuration
aws sts get-caller-identity

# Should return your AWS account details
```

## State Backend Bootstrap (One-Time Setup)

The Terraform state backend must be created manually before running Terraform for the first time. This is a one-time setup per AWS account.

### Step 1: Create S3 Bucket for State Storage

```bash
# Create S3 bucket
aws s3api create-bucket \
  --bucket petclinic-terraform-state-mumford \
  --region us-east-1

# Enable versioning (for state history)
aws s3api put-bucket-versioning \
  --bucket petclinic-terraform-state-mumford \
  --versioning-configuration Status=Enabled

# Enable encryption at rest
aws s3api put-bucket-encryption \
  --bucket petclinic-terraform-state-mumford \
  --server-side-encryption-configuration '{
    "Rules": [
      {
        "ApplyServerSideEncryptionByDefault": {
          "SSEAlgorithm": "AES256"
        }
      }
    ]
  }'

# Verify bucket exists
aws s3 ls s3://petclinic-terraform-state-mumford
```

### Step 2: Create DynamoDB Table for State Locking

```bash
# Create DynamoDB table
aws dynamodb create-table \
  --table-name petclinic-terraform-locks-mumford \
  --attribute-definitions AttributeName=LockID,AttributeType=S \
  --key-schema AttributeName=LockID,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --region us-east-1

# Verify table exists
aws dynamodb describe-table \
  --table-name petclinic-terraform-locks-mumford \
  --region us-east-1
```

**Note**: The bucket and table names are referenced in `backend.tf` files. If you change these names, update the backend configuration accordingly.

## Deploying Staging Environment

### Step 1: Navigate to Staging Directory

```bash
cd terraform/staging
```

### Step 2: Initialize Terraform

```bash
terraform init
```

This will:
- Download required provider plugins (AWS provider)
- Configure the S3 backend for remote state storage
- Prepare the working directory for Terraform operations

### Step 3: Review Planned Changes

```bash
terraform plan
```

This shows what resources Terraform will create, modify, or destroy. Review carefully before applying.

### Step 4: Apply Infrastructure Changes

```bash
terraform apply
```

Type `yes` when prompted to confirm the changes. Infrastructure provisioning takes approximately 15-20 minutes (RDS database creation is the longest step).

### Step 5: View Outputs

```bash
terraform output
```

This displays important infrastructure details like VPC ID, subnet IDs, RDS endpoint, and Secrets Manager ARN.

## Deploying Production Environment

**Note**: Production environment deployment follows the same process as staging but uses the `terraform/production/` directory.

```bash
cd terraform/production
terraform init
terraform plan
terraform apply
```

Production configuration differences:
- Longer backup retention (7 days vs 1 day)
- Final snapshot required before RDS deletion
- Separate state file in S3

## Retrieving Database Credentials

Database credentials are stored in AWS Secrets Manager for security. Retrieve them using the AWS CLI:

```bash
# Get secret value
aws secretsmanager get-secret-value \
  --secret-id petclinic/staging/database \
  --region us-east-1 \
  --query SecretString \
  --output text | jq .

# Example output (credentials will be different):
# {
#   "host": "petclinic-staging-db-mumford.xxxxx.us-east-1.rds.amazonaws.com",
#   "port": "5432",
#   "username": "petclinic",
#   "password": "[REDACTED]",
#   "dbname": "petclinic",
#   "engine": "postgres"
# }
```

### Connect to Database Using Retrieved Credentials

```bash
# Extract credentials and connect with psql
SECRET=$(aws secretsmanager get-secret-value --secret-id petclinic/staging/database --region us-east-1 --query SecretString --output text)
DB_HOST=$(echo $SECRET | jq -r .host)
DB_USER=$(echo $SECRET | jq -r .username)
DB_PASS=$(echo $SECRET | jq -r .password)
DB_NAME=$(echo $SECRET | jq -r .dbname)

# Connect to database
PGPASSWORD=$DB_PASS psql -h $DB_HOST -U $DB_USER -d $DB_NAME
```

## Terraform Outputs

After applying infrastructure, the following outputs are available:

### Network Outputs

| Output Name | Description |
|-------------|-------------|
| `vpc_id` | VPC identifier for the environment |
| `vpc_cidr` | CIDR block of the VPC (10.0.0.0/16) |
| `public_subnet_id` | Public subnet ID (for ALB and NAT Gateway) |
| `private_subnet_id` | First private subnet ID (for application and RDS) |
| `private_subnet_2_id` | Second private subnet ID (for RDS multi-AZ support) |
| `availability_zone` | Primary availability zone for resources |
| `internet_gateway_id` | ID of the Internet Gateway |
| `nat_gateway_id` | ID of the NAT Gateway |
| `nat_gateway_public_ip` | Public IP address of the NAT Gateway |

### Security Group Outputs

| Output Name | Description |
|-------------|-------------|
| `alb_security_group_id` | Security group for Application Load Balancer |
| `app_security_group_id` | Security group for application instances |
| `rds_security_group_id` | Security group for RDS database |

### Database Outputs

| Output Name | Description |
|-------------|-------------|
| `rds_endpoint` | RDS PostgreSQL connection endpoint (host:port) |
| `rds_address` | Hostname of the RDS PostgreSQL database |
| `rds_port` | RDS PostgreSQL port (5432) |
| `rds_database_name` | Database name (petclinic) |
| `rds_username` | Master username (sensitive, redacted in output) |
| `rds_arn` | ARN of the RDS PostgreSQL instance |

### Secrets Manager Outputs

| Output Name | Description |
|-------------|-------------|
| `secrets_manager_secret_arn` | ARN of the Secrets Manager secret |
| `secrets_manager_secret_name` | Name of the Secrets Manager secret |

### Environment Outputs

| Output Name | Description |
|-------------|-------------|
| `environment` | Environment name (staging or production) |
| `region` | AWS region where resources are deployed |

### Using Outputs

Use outputs in subsequent infrastructure or application configuration:

```bash
# View all outputs
terraform output

# View specific output
terraform output vpc_id
terraform output rds_endpoint

# Use in scripts
VPC_ID=$(terraform output -raw vpc_id)
RDS_ENDPOINT=$(terraform output -raw rds_endpoint)
SECRET_NAME=$(terraform output -raw secrets_manager_secret_name)
```

## Troubleshooting

### Issue: State Lock Errors

**Symptom**: `Error acquiring the state lock` when running Terraform commands.

**Cause**: Another Terraform process is running, or a previous process was interrupted and left a lock.

**Solution**:
1. Check if another team member is running Terraform
2. If not, force unlock (use with caution):
   ```bash
   terraform force-unlock <LOCK_ID>
   ```

### Issue: RDS Creation Timeout

**Symptom**: `terraform apply` appears stuck during RDS instance creation.

**Cause**: RDS database creation takes 10-15 minutes, which is normal.

**Solution**: Be patient. Monitor progress in AWS Console (RDS > Databases). If truly stuck after 30 minutes, cancel and retry.

### Issue: Connectivity Issues to RDS

**Symptom**: Cannot connect to RDS database from application or psql.

**Cause**: Security group misconfiguration or incorrect credentials.

**Solution**:
1. Verify security groups allow traffic from application security group to RDS security group on port 5432
2. Verify RDS endpoint is correct: `terraform output rds_endpoint`
3. Verify credentials in Secrets Manager are correct
4. Ensure RDS instance is in "Available" status in AWS Console

### Issue: Backend Configuration Changes

**Symptom**: Error when changing backend configuration (bucket name, key, etc.).

**Cause**: Terraform needs to reinitialize when backend configuration changes.

**Solution**:
```bash
terraform init -reconfigure
```

### Issue: `terraform fmt` Shows Changes

**Symptom**: `terraform fmt -check` fails in CI or pre-commit hooks.

**Cause**: Terraform files are not properly formatted.

**Solution**:
```bash
# Format all files
terraform fmt -recursive

# Verify formatting
terraform fmt -check -recursive
```

## Cost Estimates

Approximate monthly costs per environment (us-east-1 pricing):

| Resource | Cost per Month |
|----------|----------------|
| RDS db.t3.micro (PostgreSQL) | $15-20 |
| NAT Gateway | $32 + data transfer |
| Elastic IP | $3.65 |
| S3 State Storage | < $1 |
| Secrets Manager | $0.40 per secret |
| **Total per Environment** | **~$50-60** |

**Note**: Costs vary based on usage (data transfer, storage, etc.). Use AWS Cost Explorer for accurate tracking.

## Infrastructure Updates

To update infrastructure:

1. Modify Terraform configuration files
2. Run `terraform plan` to preview changes
3. Run `terraform apply` to apply changes
4. Commit Terraform configuration changes to version control

## Destroying Infrastructure

**⚠️ WARNING**: This permanently deletes all infrastructure. Use with extreme caution.

```bash
cd terraform/staging
terraform destroy
```

Type `yes` when prompted. Production environment has snapshot protection enabled, so you must manually delete the final RDS snapshot after destruction.

## Directory Structure

```
terraform/
├── README.md (this file)
├── staging/
│   ├── backend.tf (S3 backend configuration)
│   ├── versions.tf (provider version constraints)
│   ├── provider.tf (AWS provider settings)
│   ├── variables.tf (input variables)
│   ├── terraform.tfvars (staging-specific values)
│   ├── vpc.tf (networking resources)
│   ├── security_groups.tf (security group definitions)
│   ├── rds.tf (database resources)
│   ├── secrets.tf (Secrets Manager resources)
│   ├── outputs.tf (output values)
│   └── .gitignore (ignore state files)
└── production/
    └── (same structure as staging)
```

## Additional Resources

- [Terraform AWS Provider Documentation](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)
- [AWS RDS PostgreSQL Documentation](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/CHAP_PostgreSQL.html)
- [AWS Secrets Manager Documentation](https://docs.aws.amazon.com/secretsmanager/latest/userguide/intro.html)
- [Spring Boot RDS Connection Guide](https://docs.spring.io/spring-boot/docs/current/reference/html/data.html#data.sql.datasource)

## Support

For issues or questions:
- Check the Troubleshooting section above
- Review AWS CloudFormation events in the AWS Console
- Check Terraform state: `terraform show`
- Consult project documentation in `/docs/specs/`
