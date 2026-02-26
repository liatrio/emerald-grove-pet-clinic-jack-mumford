# Task 3.0 Proof Artifacts: Application Load Balancer and Target Group

This document contains proof artifacts demonstrating successful completion of Task 3.0.

## Overview

Task 3.0 created:
- Internet-facing Application Load Balancer in two subnets (public + private_2)
- Target Group configured for Fargate IP targets
- HTTP listener on port 80 forwarding to target group
- Health check configuration on Spring Boot Actuator endpoint
- ALB outputs for DNS name and ARNs

## ALB Infrastructure Creation

**Command:** `terraform apply`

**Resources Created:**
```
aws_lb_target_group.app: Creation complete after 2s
aws_lb.main: Creation complete after 2m44s
aws_lb_listener.http: Creation complete after 1s

Apply complete! Resources: 3 added, 0 changed, 0 destroyed.
```

**Verification:** ✅ All 3 ALB resources created successfully

## Application Load Balancer Details

**Command:** `aws elbv2 describe-load-balancers --names petclinic-staging-alb-mumford --region us-east-1`

**Output:**
```json
{
    "Name": "petclinic-staging-alb-mumford",
    "DNSName": "petclinic-staging-alb-mumford-332272699.us-east-1.elb.amazonaws.com",
    "State": "active",
    "Scheme": "internet-facing",
    "Type": "application"
}
```

**Key Details:**
- Load Balancer Name: `petclinic-staging-alb-mumford`
- DNS Name: **petclinic-staging-alb-mumford-332272699.us-east-1.elb.amazonaws.com**
- State: **active**
- Scheme: **internet-facing** (publicly accessible)
- Type: **application** (Layer 7 HTTP/HTTPS)
- ARN: `arn:aws:elasticloadbalancing:us-east-1:277802554323:loadbalancer/app/petclinic-staging-alb-mumford/907655e843cb503d`

**Subnets:**
- Public subnet: `subnet-074f5c1eb50f01f88` (us-east-1a)
- Private subnet 2: `subnet-0e8bb9894e258c95b` (us-east-1b)

**Security Group:**
- ALB security group: `sg-0957e24c57a88c061` (allows HTTP 80 and HTTPS 443 from internet)

**Verification:** ✅ ALB is active, internet-facing, and properly configured

## Target Group Configuration

**Command:** `aws elbv2 describe-target-groups --names petclinic-staging-tg-mumford --region us-east-1`

**Output:**
```json
{
    "Name": "petclinic-staging-tg-mumford",
    "Port": 8080,
    "Protocol": "HTTP",
    "TargetType": "ip",
    "HealthCheckPath": "/actuator/health",
    "HealthCheckInterval": 30
}
```

**Key Configuration:**
- Target Group Name: `petclinic-staging-tg-mumford`
- Target Type: **ip** (required for Fargate)
- Port: **8080** (Spring Boot application port)
- Protocol: **HTTP**
- VPC: `vpc-0392d6fd073c0a153`
- ARN: `arn:aws:elasticloadbalancing:us-east-1:277802554323:targetgroup/petclinic-staging-tg-mumford/0efb34053806e227`

**Health Check Settings:**
- Path: **/actuator/health** (Spring Boot Actuator endpoint)
- Protocol: **HTTP**
- Port: **traffic-port** (same as target port, 8080)
- Interval: **30 seconds**
- Timeout: **5 seconds**
- Healthy threshold: **2** consecutive successes
- Unhealthy threshold: **3** consecutive failures
- Matcher: **200** (HTTP OK status)

**Deregistration Delay:** 30 seconds (graceful shutdown)

**Verification:** ✅ Target group properly configured for Fargate IP targets with Spring Boot health checks

## HTTP Listener Configuration

**Command:** `aws elbv2 describe-listeners --load-balancer-arn <arn> --region us-east-1`

**Output:**
```json
{
    "Port": 80,
    "Protocol": "HTTP",
    "DefaultActionType": "forward"
}
```

**Key Details:**
- Listener Port: **80** (HTTP)
- Protocol: **HTTP**
- Default Action: **forward** to target group
- Target Group ARN: `arn:aws:elasticloadbalancing:us-east-1:277802554323:targetgroup/petclinic-staging-tg-mumford/0efb34053806e227`
- Listener ARN: `arn:aws:elasticloadbalancing:us-east-1:277802554323:listener/app/petclinic-staging-alb-mumford/907655e843cb503d/6c0c4ff5e5112469`

**Verification:** ✅ HTTP listener correctly forwards port 80 traffic to target group

## Target Health Status

**Command:** `aws elbv2 describe-target-health --target-group-arn <arn> --region us-east-1`

**Output:**
```json
{
    "TargetHealthDescriptions": []
}
```

**Verification:** ✅ No targets registered yet (expected, awaiting ECS service deployment in Task 4.0)

## ALB Accessibility Test

**Command:** `curl -i http://petclinic-staging-alb-mumford-332272699.us-east-1.elb.amazonaws.com/`

**Output:**
```
HTTP/1.1 503 Service Temporarily Unavailable
Server: awselb/2.0
Date: Thu, 26 Feb 2026 22:49:53 GMT
Content-Type: text/html
Content-Length: 162

<html>
<head><title>503 Service Temporarily Unavailable</title></head>
<body>
<center><h1>503 Service Temporarily Unavailable</h1></center>
</body>
</html>
```

**Analysis:**
- HTTP Status: **503 Service Temporarily Unavailable** (expected)
- Server: **awselb/2.0** (confirms AWS ALB is responding)
- Content: HTML error page indicating no healthy targets available
- **This is the correct behavior** - ALB is accessible from the internet but has no registered targets yet

**Verification:** ✅ ALB is publicly accessible via HTTP on port 80, correctly returning 503 until ECS service registers healthy targets

## Terraform Outputs

**Command:** `terraform output`

**ALB-Related Outputs:**
```
alb_arn = "arn:aws:elasticloadbalancing:us-east-1:277802554323:loadbalancer/app/petclinic-staging-alb-mumford/907655e843cb503d"
alb_dns_name = "petclinic-staging-alb-mumford-332272699.us-east-1.elb.amazonaws.com"
alb_target_group_arn = "arn:aws:elasticloadbalancing:us-east-1:277802554323:targetgroup/petclinic-staging-tg-mumford/0efb34053806e227"
alb_zone_id = "Z35SXDOTRQ7X7K"
```

**Verification:** ✅ All ALB outputs available for reference by ECS service configuration

## Files Created/Modified

1. **terraform/staging/alb.tf** (62 lines)
   - Application Load Balancer resource (internet-facing, HTTP/2 enabled)
   - Target Group resource (IP targets, health checks on /actuator/health)
   - HTTP Listener resource (port 80 → target group)

2. **terraform/staging/outputs.tf** (updated)
   - Added `alb_dns_name` output (DNS name for accessing application)
   - Added `alb_arn` output (ARN for reference)
   - Added `alb_target_group_arn` output (for ECS service registration)
   - Added `alb_zone_id` output (for potential Route 53 alias records)

## Configuration Summary

**Load Balancer:**
- Name: `petclinic-staging-alb-mumford`
- Type: Application Load Balancer (Layer 7)
- Scheme: Internet-facing
- Subnets: Public (us-east-1a) + Private 2 (us-east-1b) for high availability
- Security: ALB security group allows HTTP 80 and HTTPS 443 from 0.0.0.0/0
- Idle timeout: 60 seconds
- HTTP/2: Enabled
- Deletion protection: Disabled (for staging environment)

**Target Group:**
- Name: `petclinic-staging-tg-mumford`
- Target type: IP (required for Fargate awsvpc network mode)
- Port/Protocol: 8080/HTTP (Spring Boot default)
- Health checks: /actuator/health every 30s, 2/3 threshold
- Deregistration delay: 30s (graceful shutdown)

**Listener:**
- Port/Protocol: 80/HTTP
- Action: Forward all traffic to target group
- No path-based routing (all requests go to Pet Clinic)

**Integration Points:**
- ALB security group allows traffic from internet (port 80)
- App security group allows traffic from ALB security group (port 8080)
- Target group ARN will be used by ECS service to register tasks
- Health checks use Spring Boot Actuator endpoint for application health validation

## Cost Estimate

**Application Load Balancer Costs:**
- ALB fixed cost: $0.0225 per hour (~$16.20/month)
- LCU (Load Balancer Capacity Unit) usage: Variable based on traffic
  - New connections: 25/sec = 1 LCU
  - Active connections: 3,000/min = 1 LCU
  - Processed bytes: 1 GB/hour = 1 LCU
  - Rule evaluations: 1,000/sec = 1 LCU
- LCU cost: $0.008 per LCU-hour
- **Estimated total for staging:** ~$16-20/month (low traffic)

**Notes:**
- ALB cost is fixed regardless of ECS task scaling
- Very cost-effective compared to running dedicated load balancer instances
- Shared across all ECS tasks (scales horizontally without additional ALB cost)

## Next Steps

With the Application Load Balancer and target group configured, we can now:
1. Task 4.0: Deploy ECS service with 2 tasks
2. ECS service will automatically register task IPs with target group
3. Health checks will validate tasks are healthy before routing traffic
4. ALB DNS name will become accessible once targets are healthy

**Task 3.0 Status:** ✅ COMPLETE

All proof artifacts demonstrate successful ALB deployment with proper target group configuration and health check setup. The load balancer is publicly accessible and ready for ECS service registration.
