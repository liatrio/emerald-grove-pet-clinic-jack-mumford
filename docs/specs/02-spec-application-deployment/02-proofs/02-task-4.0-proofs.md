# Task 4.0 Proof Artifacts: ECS Service with Auto-Scaling Policies

This document contains proof artifacts demonstrating successful completion of Task 4.0.

## Overview

Task 4.0 created:
- ECS Fargate service with 2 desired tasks in private subnet
- Service registered with ALB target group
- Auto-scaling target (min 1, max 4 tasks)
- CPU-based auto-scaling policy (target 70%)
- Memory-based auto-scaling policy (target 80%)
- Scheduled scaling for off-hours cost optimization
- **Issue resolved:** IAM permissions for Secrets Manager access (task execution role)
- **Issue resolved:** Docker image platform compatibility (AMD64 for Fargate)

## ECS Service Creation

**Command:** `terraform apply`

**Resources Created:**
```
aws_ecs_service.petclinic: Creation complete after 1s
aws_appautoscaling_target.ecs_service: Creation complete after 1s
aws_appautoscaling_scheduled_action.scale_up: Creation complete after 1s
aws_appautoscaling_scheduled_action.scale_down: Creation complete after 1s
aws_appautoscaling_policy.ecs_cpu: Creation complete after 1s
aws_appautoscaling_policy.ecs_memory: Creation complete after 1s

Apply complete! Resources: 6 added, 0 changed, 0 destroyed.
```

**Verification:** ✅ All 6 resources created successfully

## Issues Encountered and Resolved

### Issue 1: Secrets Manager Access Denied

**Error:**
```
ResourceInitializationError: unable to pull secrets or registry auth: execution resource retrieval failed:
unable to retrieve secret from asm: User: arn:aws:sts::277802554323:assumed-role/petclinic-staging-task-execution-role-mumford
is not authorized to perform: secretsmanager:GetSecretValue
```

**Root Cause:** Task execution role lacked permissions to retrieve secrets from Secrets Manager at container startup.

**Resolution:**
- Added inline policy `task-execution-secrets-access` to task execution role
- Granted `secretsmanager:GetSecretValue` permission on database secret ARN
- Applied with `terraform apply` (1 resource added)

**Verification:** ✅ Tasks started successfully after IAM fix

### Issue 2: Docker Image Platform Incompatibility

**Error:**
```
CannotPullContainerError: pull image manifest has been retried 7 time(s):
image Manifest does not contain descriptor matching platform 'linux/amd64'
```

**Root Cause:** Docker image built on ARM64 (Apple Silicon) but ECS Fargate requires AMD64/x86_64 architecture.

**Resolution:**
- Rebuilt Docker image with `--platform linux/amd64` flag
- Pushed AMD64 image to ECR (digest: sha256:8d5086fa00e0c30471f6f85c9d2f69d5ec8cae44b2cdffcd2c4e99549aef1b40)
- Forced ECS service redeployment with `aws ecs update-service --force-new-deployment`

**Verification:** ✅ Tasks pulled AMD64 image and started successfully

## ECS Service Details

**Command:** `aws ecs describe-services --cluster petclinic-staging-cluster-mumford --services petclinic-staging-service-mumford`

**Output:**
```json
{
    "Name": "petclinic-staging-service-mumford",
    "Status": "ACTIVE",
    "DesiredCount": 2,
    "RunningCount": 2,
    "PendingCount": 0
}
```

**Key Details:**
- Service Name: `petclinic-staging-service-mumford`
- Status: **ACTIVE**
- Desired Count: **2 tasks**
- Running Count: **2 tasks** ✅
- Pending Count: **0 tasks**
- Launch Type: **FARGATE**
- Platform Version: **LATEST**
- Deployment Strategy: Recreate (max 100%, min 0%)

**Network Configuration:**
- Subnet: `subnet-0e8c170ff873e3cdf` (private subnet)
- Security Group: `sg-0399d3d38eb6e2355` (app security group)
- Assign Public IP: **true** (for NAT Gateway access to ECR/Secrets Manager)

**Load Balancer Integration:**
- Target Group ARN: `arn:aws:elasticloadbalancing:us-east-1:277802554323:targetgroup/petclinic-staging-tg-mumford/0efb34053806e227`
- Container Name: **petclinic**
- Container Port: **8080**
- Health Check Grace Period: **60 seconds**

**Verification:** ✅ ECS service is active with 2/2 tasks running

## Running Tasks

**Command:** `aws ecs list-tasks --cluster petclinic-staging-cluster-mumford --service-name petclinic-staging-service-mumford`

**Task ARNs:**
```
arn:aws:ecs:us-east-1:277802554323:task/petclinic-staging-cluster-mumford/562bed25e27044e7bb2662c149c8202a
arn:aws:ecs:us-east-1:277802554323:task/petclinic-staging-cluster-mumford/caf5f766f86f403ea5930100a069c6a0
```

**Task Status:**
- Task 1: **RUNNING** with health status **HEALTHY**
- Task 2: **RUNNING** with health status **HEALTHY**

**Verification:** ✅ Both tasks are running and healthy

## ALB Target Health

**Command:** `aws elbv2 describe-target-health --target-group-arn <arn>`

**Output:**
```json
[
    {
        "Target": "10.0.2.248",
        "Port": 8080,
        "HealthState": "healthy",
        "Reason": null
    },
    {
        "Target": "10.0.2.122",
        "Port": 8080,
        "HealthState": "healthy",
        "Reason": null
    }
]
```

**Key Details:**
- Target 1 (10.0.2.248): **healthy** ✅
- Target 2 (10.0.2.122): **healthy** ✅
- Both targets passing health checks on `/actuator/health`

**Verification:** ✅ All targets registered and healthy in ALB target group

## Application Accessibility

**Command:** `curl -I http://petclinic-staging-alb-mumford-332272699.us-east-1.elb.amazonaws.com/`

**Output:**
```
HTTP/1.1 200
Date: Thu, 26 Feb 2026 23:05:16 GMT
Content-Type: text/html;charset=UTF-8
Content-Language: en
```

**Analysis:**
- HTTP Status: **200 OK** ✅
- Content-Type: **text/html;charset=UTF-8** (Pet Clinic homepage)
- Content-Language: **en** (English)
- Application is publicly accessible via ALB DNS name
- Requests are being routed to healthy targets

**Verification:** ✅ Pet Clinic application is accessible and serving content correctly

## Auto-Scaling Configuration

### Auto-Scaling Target

**Resource:** `aws_appautoscaling_target.ecs_service`

**Configuration:**
- Resource ID: `service/petclinic-staging-cluster-mumford/petclinic-staging-service-mumford`
- Scalable Dimension: `ecs:service:DesiredCount`
- Service Namespace: `ecs`
- Min Capacity: **1 task**
- Max Capacity: **4 tasks**

**Verification:** ✅ Auto-scaling target configured for ECS service

### CPU Auto-Scaling Policy

**Command:** `aws application-autoscaling describe-scaling-policies`

**Output:**
```json
{
    "Name": "petclinic-staging-cpu-autoscaling",
    "Type": "TargetTrackingScaling",
    "TargetValue": 70.0,
    "MetricType": "ECSServiceAverageCPUUtilization"
}
```

**Configuration:**
- Policy Name: `petclinic-staging-cpu-autoscaling`
- Policy Type: **Target Tracking Scaling**
- Metric: **ECSServiceAverageCPUUtilization**
- Target Value: **70%**
- Scale-in/Scale-out: Automatic based on CPU utilization

**Behavior:**
- If average CPU > 70%: Scale OUT (add tasks, up to max 4)
- If average CPU < 70%: Scale IN (remove tasks, down to min 1)

**Verification:** ✅ CPU auto-scaling policy active

### Memory Auto-Scaling Policy

**Output:**
```json
{
    "Name": "petclinic-staging-memory-autoscaling",
    "Type": "TargetTrackingScaling",
    "TargetValue": 80.0,
    "MetricType": "ECSServiceAverageMemoryUtilization"
}
```

**Configuration:**
- Policy Name: `petclinic-staging-memory-autoscaling`
- Policy Type: **Target Tracking Scaling**
- Metric: **ECSServiceAverageMemoryUtilization**
- Target Value: **80%**
- Scale-in/Scale-out: Automatic based on memory utilization

**Behavior:**
- If average memory > 80%: Scale OUT (add tasks, up to max 4)
- If average memory < 80%: Scale IN (remove tasks, down to min 1)

**Verification:** ✅ Memory auto-scaling policy active

## Scheduled Scaling Actions

### Scale Down - Off-Hours

**Command:** `aws application-autoscaling describe-scheduled-actions`

**Output:**
```json
{
    "Name": "petclinic-staging-scale-down",
    "Schedule": "cron(0 22 ? * MON-FRI *)",
    "MinCapacity": 0,
    "MaxCapacity": 0
}
```

**Configuration:**
- Schedule: **cron(0 22 ? * MON-FRI *)** (10 PM UTC Monday-Friday)
- Note: 10 PM UTC = 5 PM EST (or 6 PM EDT depending on DST)
- Action: Set min/max capacity to **0** (stop all tasks)
- Purpose: Cost optimization during off-hours

**Verification:** ✅ Scheduled scale-down action configured

### Scale Up - Business Hours

**Output:**
```json
{
    "Name": "petclinic-staging-scale-up",
    "Schedule": "cron(0 6 ? * MON-FRI *)",
    "MinCapacity": 1,
    "MaxCapacity": 4
}
```

**Configuration:**
- Schedule: **cron(0 6 ? * MON-FRI *)** (6 AM UTC Monday-Friday)
- Note: 6 AM UTC = 1 AM EST (or 2 AM EDT depending on DST)
- Action: Set min capacity to **1**, max capacity to **4**
- Purpose: Resume service before business hours

**Verification:** ✅ Scheduled scale-up action configured

## Files Created/Modified

1. **terraform/staging/variables.tf** (updated)
   - Added `ecs_desired_count` (default: 2)
   - Added `ecs_min_count` (default: 1)
   - Added `ecs_max_count` (default: 4)
   - Added `cpu_target_value` (default: 70)
   - Added `memory_target_value` (default: 80)

2. **terraform/staging/ecs.tf** (updated)
   - Added ECS service resource with ALB integration
   - Added auto-scaling target
   - Added CPU auto-scaling policy
   - Added memory auto-scaling policy
   - Added scheduled scale-down action
   - Added scheduled scale-up action

3. **terraform/staging/iam.tf** (updated)
   - Added `task_execution_secrets_access` inline policy to task execution role
   - Grants `secretsmanager:GetSecretValue` permission for container startup

4. **terraform/staging/outputs.tf** (updated)
   - Added `ecs_cluster_name` output
   - Added `ecs_cluster_arn` output
   - Added `ecs_service_name` output
   - Added `ecs_service_id` output
   - Added `ecs_task_definition_arn` output

5. **Docker Image** (rebuilt and pushed)
   - Platform: linux/amd64 (ECS Fargate compatible)
   - Image: 277802554323.dkr.ecr.us-east-1.amazonaws.com/petclinic-staging-ecr-mumford:latest
   - Digest: sha256:8d5086fa00e0c30471f6f85c9d2f69d5ec8cae44b2cdffcd2c4e99549aef1b40

## Configuration Summary

**ECS Service:**
- Desired tasks: 2
- Launch type: Fargate
- Platform: LATEST
- Deployment: Recreate strategy (stop all, start new)
- Network: Private subnet with public IP for NAT Gateway access
- Load balancer: Integrated with ALB target group
- Health check grace: 60 seconds

**Auto-Scaling:**
- Base capacity: 1-4 tasks
- CPU target: 70% utilization
- Memory target: 80% utilization
- Scheduled downtime: 10 PM UTC weekdays (all tasks stopped)
- Scheduled uptime: 6 AM UTC weekdays (tasks restored)

**Security:**
- Tasks run in private subnet (no direct internet access)
- Public IP assigned for outbound access via NAT Gateway (ECR, Secrets Manager)
- App security group allows traffic from ALB only (port 8080)
- IAM task execution role retrieves secrets at startup
- IAM task role for application runtime AWS access

## Cost Estimate

**ECS Fargate Costs (2 tasks, 24/7):**
- 2 tasks × 0.5 vCPU × $0.04048/hour = $0.08096/hour
- 2 tasks × 1 GB memory × $0.004445/hour = $0.00889/hour
- **Total compute:** ~$0.09/hour = ~$64.80/month

**With Scheduled Scaling:**
- Assume tasks run 50% of the time (scaled to 0 off-hours)
- **Optimized compute:** ~$32.40/month

**Auto-Scaling Cost Impact:**
- Scales up to 4 tasks during high load (2x cost temporarily)
- Scales down to 0 during off-hours (0 cost)
- Net savings: ~50% reduction in compute costs for staging

**CloudWatch Metrics/Alarms:**
- Auto-scaling policies use CloudWatch metrics (included in Container Insights)
- Minimal additional cost (<$1/month)

**Total ECS Service Cost (staging):** ~$32-65/month depending on scaling

## Next Steps

With the ECS service deployed and auto-scaling configured, we can now:
1. Task 5.0: Validate end-to-end application access
2. Test database connectivity and data persistence
3. Verify CloudWatch logs and X-Ray tracing
4. Validate health checks and auto-scaling behavior

**Task 4.0 Status:** ✅ COMPLETE

All proof artifacts demonstrate successful ECS service deployment with 2 healthy tasks, ALB integration, CPU/memory auto-scaling, and scheduled scaling for cost optimization. Both IAM permissions and Docker platform issues were identified and resolved during implementation.
