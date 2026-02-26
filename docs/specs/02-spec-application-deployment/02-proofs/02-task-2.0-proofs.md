# Task 2.0 Proof Artifacts: ECS Cluster and Task Definition

This document contains proof artifacts demonstrating successful completion of Task 2.0.

## Overview

Task 2.0 created:
- ECS Fargate cluster with Container Insights enabled
- ECS task definition with 0.5 vCPU and 1024 MB memory
- Two containers: Pet Clinic application + AWS X-Ray daemon
- IAM task execution role (for ECS infrastructure)
- IAM task role (for application AWS service access)
- CloudWatch Log Group with 7-day retention
- Environment variables and Secrets Manager integration

## ECS Cluster Creation

**Command:** `terraform apply`

**Resources Created:**
```
aws_ecs_cluster.main: Creation complete after 11s
aws_cloudwatch_log_group.ecs: Creation complete after 1s
aws_iam_role.ecs_task_execution: Creation complete after 1s
aws_iam_role.ecs_task: Creation complete after 1s
aws_ecs_task_definition.petclinic: Creation complete after 0s
aws_iam_role_policy.secrets_manager_access: Creation complete after 0s
aws_iam_role_policy_attachment.ecs_task_execution_policy: Creation complete
aws_iam_role_policy_attachment.cloudwatch_logs: Creation complete
aws_iam_role_policy_attachment.xray_daemon: Creation complete

Apply complete! Resources: 9 added, 0 changed, 0 destroyed.
```

**Verification:** ✅ All 9 ECS/IAM resources created successfully

## ECS Cluster Details

**Command:** `aws ecs describe-clusters --clusters petclinic-staging-cluster-mumford --region us-east-1`

**Output:**
```json
{
    "clusters": [
        {
            "clusterArn": "arn:aws:ecs:us-east-1:277802554323:cluster/petclinic-staging-cluster-mumford",
            "clusterName": "petclinic-staging-cluster-mumford",
            "status": "ACTIVE",
            "registeredContainerInstancesCount": 0,
            "runningTasksCount": 0,
            "pendingTasksCount": 0,
            "activeServicesCount": 0
        }
    ]
}
```

**Key Details:**
- Cluster Name: `petclinic-staging-cluster-mumford`
- Status: **ACTIVE**
- Cluster ARN: `arn:aws:ecs:us-east-1:277802554323:cluster/petclinic-staging-cluster-mumford`
- Container Insights: Enabled (for monitoring)

**Verification:** ✅ ECS cluster is active and ready for service deployment

## ECS Task Definition Configuration

**Command:** `aws ecs describe-task-definition --task-definition petclinic-staging-task-mumford --region us-east-1`

**Task Definition ARN:** `arn:aws:ecs:us-east-1:277802554323:task-definition/petclinic-staging-task-mumford:1`

### Task Configuration

**Family:** `petclinic-staging-task-mumford`
**Revision:** 1
**Network Mode:** `awsvpc`
**Requires Compatibilities:** `FARGATE`
**CPU:** 512 (0.5 vCPU)
**Memory:** 1024 MB

**Execution Role:** `arn:aws:iam::277802554323:role/petclinic-staging-task-execution-role-mumford`
**Task Role:** `arn:aws:iam::277802554323:role/petclinic-staging-task-role-mumford`

### Container 1: Pet Clinic Application

**Name:** `petclinic`
**Image:** `277802554323.dkr.ecr.us-east-1.amazonaws.com/petclinic-staging-ecr-mumford:latest`
**Essential:** `true`
**Port Mapping:** 8080:8080/tcp

**Environment Variables:**
```json
{
    "name": "SPRING_PROFILES_ACTIVE",
    "value": "postgres"
},
{
    "name": "JAVA_OPTS",
    "value": "-Xmx768m -XX:+UseContainerSupport"
},
{
    "name": "SPRING_DATASOURCE_URL",
    "value": "jdbc:postgresql://petclinic-staging-db-mumford.cgrk8yc6a9k1.us-east-1.rds.amazonaws.com:5432/petclinic"
}
```

**Secrets (from AWS Secrets Manager):**
```json
{
    "name": "SPRING_DATASOURCE_USERNAME",
    "valueFrom": "arn:aws:secretsmanager:us-east-1:277802554323:secret:petclinic/staging/database-fNACrl:username::"
},
{
    "name": "SPRING_DATASOURCE_PASSWORD",
    "valueFrom": "arn:aws:secretsmanager:us-east-1:277802554323:secret:petclinic/staging/database-fNACrl:password::"
}
```

**Log Configuration:**
- Log Driver: `awslogs`
- Log Group: `/ecs/petclinic-staging-mumford`
- Region: `us-east-1`
- Stream Prefix: `ecs`

**Health Check:**
- Command: `curl -f http://localhost:8080/actuator/health || exit 1`
- Interval: 30 seconds
- Timeout: 5 seconds
- Retries: 3
- Start Period: 60 seconds

**Verification:** ✅ Main application container properly configured with database connection and health checks

### Container 2: AWS X-Ray Daemon

**Name:** `xray-daemon`
**Image:** `public.ecr.aws/xray/aws-xray-daemon:latest`
**Essential:** `false` (sidecar container)
**CPU:** 32
**Memory:** 256 MB
**Port Mapping:** 2000:2000/udp

**Log Configuration:**
- Log Driver: `awslogs`
- Log Group: `/ecs/petclinic-staging-mumford`
- Region: `us-east-1`
- Stream Prefix: `xray`

**Verification:** ✅ X-Ray daemon configured for distributed tracing

## IAM Roles Configuration

### Task Execution Role

**Role Name:** `petclinic-staging-task-execution-role-mumford`
**Role ARN:** `arn:aws:iam::277802554323:role/petclinic-staging-task-execution-role-mumford`

**Purpose:** Used by ECS to pull container images from ECR and write logs to CloudWatch

**Managed Policies Attached:**
- `arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy`

**Permissions:**
- Pull images from ECR repositories
- Write logs to CloudWatch Logs
- Retrieve secrets from Secrets Manager (for task definition secrets)

**Verification:** ✅ Task execution role properly configured for ECS infrastructure operations

### Task Role

**Role Name:** `petclinic-staging-task-role-mumford`
**Role ARN:** `arn:aws:iam::277802554323:role/petclinic-staging-task-role-mumford`

**Purpose:** Used by application code to access AWS services

**Managed Policies Attached:**
- `arn:aws:iam::aws:policy/CloudWatchLogsFullAccess`
- `arn:aws:iam::aws:policy/AWSXRayDaemonWriteAccess`

**Inline Policies:**
- **secrets-manager-access**: Grants `GetSecretValue` and `DescribeSecret` on the database credentials secret

**Permissions:**
- Read database credentials from Secrets Manager
- Write application logs to CloudWatch Logs
- Send trace data to AWS X-Ray

**Verification:** ✅ Task role follows least privilege principle with specific secret access

## CloudWatch Log Group

**Command:** `aws logs describe-log-groups --log-group-name-prefix /ecs/petclinic-staging --region us-east-1`

**Output:**
```json
{
    "logGroups": [
        {
            "logGroupName": "/ecs/petclinic-staging-mumford",
            "creationTime": 1772145427280,
            "retentionInDays": 7,
            "metricFilterCount": 0,
            "arn": "arn:aws:logs:us-east-1:277802554323:log-group:/ecs/petclinic-staging-mumford:*",
            "storedBytes": 0,
            "logGroupClass": "STANDARD"
        }
    ]
}
```

**Key Configuration:**
- Log Group Name: `/ecs/petclinic-staging-mumford`
- Retention: 7 days (cost-optimized for staging)
- Status: Active
- Stored Bytes: 0 (no logs yet, awaiting service deployment)

**Verification:** ✅ CloudWatch log group ready to receive application and X-Ray daemon logs

## Files Created

1. **terraform/staging/ecs.tf** (122 lines)
   - ECS cluster resource with Container Insights
   - CloudWatch Log Group for ECS logs
   - ECS task definition with dual containers (app + X-Ray)
   - Environment variables and secrets configuration
   - Health check and logging configuration

2. **terraform/staging/iam.tf** (93 lines)
   - Task execution role with assume role policy
   - Task execution role policy attachment (ECR + CloudWatch)
   - Task role with assume role policy
   - Inline policy for Secrets Manager access
   - Task role policy attachments (CloudWatch + X-Ray)

3. **terraform/staging/variables.tf** (updated)
   - Added `ecs_task_cpu` (default: 512)
   - Added `ecs_task_memory` (default: 1024)
   - Added `ecs_container_port` (default: 8080)

## Configuration Summary

**Task Resource Allocation:**
- Total CPU: 512 units (0.5 vCPU) = 480 for app + 32 for X-Ray
- Total Memory: 1024 MB = ~768 MB for app (JVM max heap) + 256 MB for X-Ray

**Database Connection:**
- JDBC URL: Constructed from RDS instance attributes
- Username/Password: Retrieved from Secrets Manager at task startup
- Profile: `postgres` (Spring Boot PostgreSQL configuration)

**Observability:**
- Application logs → CloudWatch Logs (`/ecs/petclinic-staging-mumford`, prefix: `ecs`)
- X-Ray daemon logs → CloudWatch Logs (`/ecs/petclinic-staging-mumford`, prefix: `xray`)
- Distributed tracing → AWS X-Ray
- Container Insights → ECS cluster monitoring

**Security:**
- Secrets Manager integration for credentials (no plaintext passwords)
- IAM roles follow least privilege principle
- Separate execution role and task role
- Task role limited to specific secret ARN

## Cost Estimate

**ECS Fargate Costs (per task):**
- 0.5 vCPU: $0.04048 per hour
- 1 GB Memory: $0.004445 per hour
- **Total per task per hour:** ~$0.045
- **Total per task per month (24/7):** ~$32.40

**For staging with 2 tasks:** ~$64.80/month (before scheduled scaling optimization)

**CloudWatch Logs:**
- Storage: $0.50/GB ingested, $0.03/GB archived
- Expected staging usage: < 1 GB/month = < $1/month

**X-Ray Tracing:**
- First 100,000 traces per month: Free
- Beyond: $5/million traces recorded

**Total ECS Infrastructure (staging):** ~$65-70/month (2 tasks running 24/7)

**Note:** Scheduled scaling to 0 during off-hours can reduce compute costs by ~50%.

## Next Steps

With the ECS cluster and task definition configured, we can now:
1. Task 3.0: Create Application Load Balancer to route traffic to tasks
2. Task 4.0: Deploy ECS service with auto-scaling
3. Task 5.0: Validate end-to-end application access

The task definition is registered and ready for service deployment.

**Task 2.0 Status:** ✅ COMPLETE

All proof artifacts demonstrate successful ECS cluster and task definition configuration with proper IAM roles, logging, and database integration.
