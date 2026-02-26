# Task 3.0 Proof Artifacts: Configure Security Groups for Database and Application

This document contains proof artifacts demonstrating successful completion of Task 3.0.

## CLI Output: Terraform Apply Success

**Command:** `terraform apply -auto-approve`

**Summary Output:**
```
Terraform will perform the following actions:
  + aws_security_group.alb
  + aws_security_group.app
  + aws_security_group.rds

Plan: 3 to add, 0 to change, 0 to destroy.

aws_security_group.alb: Creation complete after 3s [id=sg-0957e24c57a88c061]
aws_security_group.app: Creation complete after 3s [id=sg-0399d3d38eb6e2355]
aws_security_group.rds: Creation complete after 3s [id=sg-09322b84d1d8839e4]

Apply complete! Resources: 3 added, 0 changed, 0 destroyed.
```

**Verification:** ✅ All 3 security groups created successfully

## CLI Output: Terraform State List

**Command:** `terraform state list | grep aws_security_group`

**Output:**
```
aws_security_group.alb
aws_security_group.app
aws_security_group.rds
```

**Verification:** ✅ All security groups tracked in Terraform state

## Security Group Details

### 1. Application Load Balancer Security Group

**ID:** `sg-0957e24c57a88c061`
**Name:** `petclinic-staging-alb-sg-mumford`
**Description:** Security group for Application Load Balancer
**VPC:** `vpc-0392d6fd073c0a153`

**Ingress Rules:**
- **HTTP**: Port 80, Protocol TCP, Source: 0.0.0.0/0 (internet)
- **HTTPS**: Port 443, Protocol TCP, Source: 0.0.0.0/0 (internet)

**Egress Rules:**
- **All traffic**: All ports, All protocols, Destination: 0.0.0.0/0

**Purpose:** Allow internet traffic to reach the Application Load Balancer on HTTP and HTTPS ports

**Verification:** ✅ ALB security group allows HTTP (80) and HTTPS (443) from internet

### 2. Application Security Group

**ID:** `sg-0399d3d38eb6e2355`
**Name:** `petclinic-staging-app-sg-mumford`
**Description:** Security group for application instances
**VPC:** `vpc-0392d6fd073c0a153`

**Ingress Rules:**
- **Spring Boot**: Port 8080, Protocol TCP, Source: sg-0957e24c57a88c061 (ALB security group)

**Egress Rules:**
- **All traffic**: All ports, All protocols, Destination: 0.0.0.0/0

**Purpose:** Allow traffic from ALB to application instances on Spring Boot's default port (8080)

**Verification:** ✅ Application security group allows port 8080 from ALB security group only

### 3. RDS Database Security Group

**ID:** `sg-09322b84d1d8839e4`
**Name:** `petclinic-staging-rds-sg-mumford`
**Description:** Security group for RDS PostgreSQL database
**VPC:** `vpc-0392d6fd073c0a153`

**Ingress Rules:**
- **PostgreSQL**: Port 5432, Protocol TCP, Source: sg-0399d3d38eb6e2355 (application security group)

**Egress Rules:**
- **All traffic**: All ports, All protocols, Destination: 0.0.0.0/0

**Purpose:** Allow database traffic from application instances only (principle of least privilege)

**Verification:** ✅ RDS security group allows PostgreSQL (5432) from application security group only

## Security Architecture

```
Internet
   │
   ├─ HTTP (80)  ─────────┐
   └─ HTTPS (443) ────────┤
                          │
                     [ ALB SG ]
                     sg-0957...
                          │
                    Port 8080
                          │
                     [ APP SG ]
                     sg-0399...
                          │
                    Port 5432
                          │
                     [ RDS SG ]
                     sg-0932...
                          │
                    PostgreSQL
                      Database
```

**Security Principles Applied:**
- ✅ **Least Privilege**: Each layer only allows necessary traffic
- ✅ **Defense in Depth**: Multiple security layers (ALB → App → RDS)
- ✅ **No Direct Database Access**: RDS only accessible from application layer
- ✅ **Controlled Entry Points**: Only ALB exposed to internet

## Terraform Configuration

**File:** `terraform/staging/security_groups.tf`

**Key Features:**
- Security group references using `aws_security_group.{name}.id`
- Proper use of `security_groups` parameter for inter-group communication
- Descriptive names and descriptions for each rule
- Consistent naming convention with -mumford suffix
- All resources tagged with environment metadata

**Verification:** ✅ Security groups defined with proper dependencies and references

## Configuration Validation

**Command:** `terraform validate`

**Output:**
```
Success! The configuration is valid.
```

**Command:** `terraform fmt`

**Output:**
```
(No output - all files properly formatted)
```

**Verification:** ✅ Terraform configuration is valid and properly formatted

## Naming Convention Compliance

All security groups follow the naming pattern: `petclinic-{environment}-{resource-type}-sg-mumford`

**Examples:**
- ✅ `petclinic-staging-alb-sg-mumford`
- ✅ `petclinic-staging-app-sg-mumford`
- ✅ `petclinic-staging-rds-sg-mumford`

**Verification:** ✅ All security groups follow consistent naming convention

## AWS Console Verification

To verify in AWS Console:

1. **EC2 Dashboard** → Navigate to Security Groups
2. **Filter by VPC:** `vpc-0392d6fd073c0a153`
3. **Verify ALB Security Group:**
   - Name: `petclinic-staging-alb-sg-mumford`
   - Inbound: HTTP (80) and HTTPS (443) from 0.0.0.0/0
   - Outbound: All traffic to 0.0.0.0/0

4. **Verify Application Security Group:**
   - Name: `petclinic-staging-app-sg-mumford`
   - Inbound: Port 8080 from ALB security group (sg-0957e24c57a88c061)
   - Outbound: All traffic to 0.0.0.0/0

5. **Verify RDS Security Group:**
   - Name: `petclinic-staging-rds-sg-mumford`
   - Inbound: Port 5432 from app security group (sg-0399d3d38eb6e2355)
   - Outbound: All traffic to 0.0.0.0/0

## Resource Tagging

All security groups include default tags from provider configuration:

```hcl
default_tags {
  tags = {
    Project     = "emerald-grove-pet-clinic"
    Environment = "staging"
    ManagedBy   = "terraform"
    Owner       = "mumford"
  }
}
```

Additional resource-specific Name tags applied for easy identification.

**Verification:** ✅ All security groups tagged consistently

## Summary

All proof artifacts for Task 3.0 have been successfully demonstrated:

- [x] `security_groups.tf` file created
- [x] ALB security group created with HTTP (80) and HTTPS (443) ingress from internet
- [x] Application security group created with port 8080 ingress from ALB
- [x] RDS security group created with PostgreSQL (5432) ingress from application
- [x] All security groups have egress rules allowing outbound traffic
- [x] All security groups properly tagged
- [x] Configuration validated with `terraform validate`
- [x] Configuration formatted with `terraform fmt`
- [x] Terraform plan reviewed (3 resources to add)
- [x] Terraform apply executed successfully
- [x] All 3 security groups created and tracked in state
- [x] Security architecture follows least privilege principle
- [x] Naming conventions followed consistently

**Task 3.0 Status:** ✅ COMPLETE
