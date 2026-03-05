# 01 Questions Round 1 - AWS Core Infrastructure

Please answer each question below (select one or more options, or add your own notes). Feel free to add additional context under any question.

## 1. AWS Account & Region

What AWS account and region will you be deploying to?

- [x] (A) Using an existing AWS account with specific region preference (specify which region)
- [ ] (B) Creating a new AWS account for this project
- [ ] (C) Using AWS Organizations with multiple accounts (dev/staging/prod)
- [ ] (D) Using a single account with environment separation via tags/naming
- [ ] (E) Other (describe)

**Please specify your preferred AWS region:** _____________

## 2. Database Choice

Which database engine should we use for production?

- [x] (A) Amazon RDS PostgreSQL (recommended - already have schema and config)
- [ ] (B) Amazon RDS MySQL (already have schema and config)
- [ ] (C) Amazon Aurora PostgreSQL (higher performance, serverless option available)
- [ ] (D) Amazon Aurora MySQL (higher performance, serverless option available)
- [ ] (E) Other (describe)

**Database size/instance type preference:**
- [ ] Small workload (db.t3.micro or db.t4g.micro)
- [ ] Medium workload (db.t3.small or db.t4g.small)
- [ ] Production workload (db.m5.large or higher)
- [x] Let Terraform set reasonable defaults

## 3. Network Architecture

What networking setup do you need?

- [ ] (A) Simple VPC with public subnets only (less secure, simpler, lower cost)
- [x] (B) VPC with public and private subnets (recommended - app in private, ALB in public)
- [ ] (C) Multi-AZ setup for high availability (production-grade)
- [ ] (D) Single AZ for cost savings (development/staging)
- [ ] (E) Other (describe)

## 4. Terraform State Management

How should Terraform state be stored?

- [ ] (A) Local state file (simple but not team-friendly, not recommended)
- [x] (B) S3 backend with DynamoDB locking (recommended for team collaboration)
- [ ] (C) Terraform Cloud (managed state, easier collaboration)
- [ ] (D) Existing Terraform state setup (specify details)
- [ ] (E) Other (describe)

## 5. Environment Strategy

How many environments do you need infrastructure for?

- [ ] (A) Single environment (production only)
- [x] (B) Two environments (staging + production)
- [ ] (C) Three environments (dev + staging + production)
- [ ] (D) Start with one, make it reusable for multiple environments later
- [ ] (E) Other (describe)

## 6. Database Backup & Disaster Recovery

What backup and DR requirements do you have?

- [ ] (A) Automated daily backups with 7-day retention (AWS default)
- [ ] (B) Automated daily backups with 30-day retention
- [ ] (C) Automated backups + manual snapshots before deployments
- [ ] (D) Multi-region replication for disaster recovery
- [x] (E) Minimal backups for dev environment
- [ ] (F) Other (describe)

## 7. Security & Access Control

How should database access be secured?

- [ ] (A) Security groups allowing access only from application subnets
- [x] (B) Security groups + AWS Secrets Manager for credentials
- [ ] (C) Security groups + IAM database authentication
- [ ] (D) Include bastion host for administrative access
- [ ] (E) No direct external access, app-only access
- [ ] (F) Other (describe)

## 8. Database Schema Initialization

How should the database schema be initialized?

- [ ] (A) Manual execution of SQL scripts from `/src/main/resources/db/[mysql|postgres]/`
- [ ] (B) Automated via Terraform null_resource executing scripts
- [x] (C) Application handles schema creation on first boot (Spring Boot default)
- [ ] (D) Separate database migration tool (Flyway/Liquibase) - to be added later
- [ ] (E) Other (describe)

## 9. Cost Optimization

What's your priority for cost vs. availability?

- [ ] (A) Cost-optimized (single AZ, smaller instances, dev/test workload)
- [ ] (B) Balanced (multi-AZ for RDS, reasonable instance sizes)
- [ ] (C) High availability (multi-AZ everything, larger instances, production-grade)
- [x] (D) Start minimal, scale up as needed
- [ ] (E) Other (describe)

## 10. Existing AWS Resources

Do you have any existing AWS resources we need to integrate with or avoid conflicts?

- [x] (A) Starting fresh, no existing resources
- [ ] (B) Existing VPC to use (provide VPC ID)
- [ ] (C) Existing security groups or networking setup
- [ ] (D) Existing S3 buckets or IAM roles
- [ ] (E) Other (describe)

**If yes, please provide details:** _____________

## 11. Proof Artifacts

What would you like to see as proof that the infrastructure is working?

- [ ] (A) Terraform apply output showing all resources created successfully
- [ ] (B) AWS Console screenshots showing VPC, RDS, security groups
- [ ] (C) Database connectivity test (connect and run simple query)
- [ ] (D) Terraform state file showing infrastructure matches desired state
- [x] (E) All of the above
- [ ] (F) Other (describe)

## 12. Terraform Structure Preferences

How should the Terraform code be organized?

- [ ] (A) Single terraform directory with all resources
- [ ] (B) Modular structure (separate modules for networking, database, etc.)
- [x] (C) Environment-based directories (terraform/dev, terraform/prod)
- [ ] (D) Workspace-based (single code, multiple workspaces)
- [ ] (E) Let you decide based on best practices
- [ ] (F) Other (describe)

---

## Additional Context

Please provide any additional information that would help with the infrastructure design:

- Compliance requirements (HIPAA, SOC2, etc.)?
- Expected traffic/load patterns?
- Budget constraints?
- Timeline constraints?
- Any other specific requirements?

---

**Next Steps:** Once you've answered these questions, save this file and let me know. I'll review your answers and may ask follow-up questions if needed before generating the specification.
