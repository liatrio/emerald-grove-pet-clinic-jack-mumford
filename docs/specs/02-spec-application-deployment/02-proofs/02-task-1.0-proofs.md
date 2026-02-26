# Task 1.0 Proof Artifacts: Container Image and ECR Repository

This document contains proof artifacts demonstrating successful completion of Task 1.0.

## Overview

Task 1.0 created:
- Multi-stage Dockerfile for containerizing Pet Clinic application
- Docker image with Java 17 runtime (multi-platform compatible)
- AWS ECR repository with lifecycle policy
- Successfully pushed Docker image to ECR

## Dockerfile Configuration

**File:** `Dockerfile` (repository root)

**Key Features:**
- Multi-stage build: Maven build stage + JRE runtime stage
- Base images: `maven:3.9-eclipse-temurin-17` (build), `eclipse-temurin:17-jre` (runtime)
- Non-root user (`appuser`) for security
- Port 8080 exposed for Spring Boot application
- JVM options: `-Xmx768m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0`
- Health check on `/actuator/health` endpoint
- Final image size: ~332 MB

**Verification:** ✅ Dockerfile exists and follows best practices

## .dockerignore Configuration

**File:** `.dockerignore` (repository root)

**Key Exclusions:**
- Build artifacts (`target/`, `*.jar`)
- IDE files (`.idea/`, `.vscode/`)
- Documentation (`*.md`, `docs/`)
- Terraform files (`terraform/`, `*.tf`)
- Git files (`.git/`, `.gitignore`)
- CI/CD files (`.github/`)

**Verification:** ✅ .dockerignore properly configured to minimize build context

## Docker Build Success

**Command:** `docker build -t petclinic:latest .`

**Output Summary:**
```
[build 8/8] RUN mvn clean package -DskipTests -B
[INFO] BUILD SUCCESS
[INFO] Total time:  18.689 s
[INFO] Finished at: 2026-02-26T22:18:35Z
[INFO] Building jar: /app/target/spring-petclinic-4.0.0-SNAPSHOT.jar

[stage-1 6/6] RUN chown -R appuser:appgroup /app
exporting to image
exporting layers 4.4s done
exporting manifest sha256:664b186fb4c3a4a862765461dd31ff2d7d27dba8708ac7dba76cd0bb9838be39 done
naming to docker.io/library/petclinic:latest done
unpacking to docker.io/library/petclinic:latest 1.8s done
```

**Verification:** ✅ Docker build completed successfully in ~22 seconds

## ECR Repository Creation

**Command:** `terraform apply`

**Terraform Output:**
```
aws_ecr_repository.petclinic: Creating...
aws_ecr_repository.petclinic: Creation complete after 1s [id=petclinic-staging-ecr-mumford]
aws_ecr_lifecycle_policy.petclinic: Creating...
aws_ecr_lifecycle_policy.petclinic: Creation complete after 0s [id=petclinic-staging-ecr-mumford]

Apply complete! Resources: 2 added, 0 changed, 0 destroyed.

Outputs:
ecr_repository_arn = "arn:aws:ecr:us-east-1:277802554323:repository/petclinic-staging-ecr-mumford"
ecr_repository_url = "277802554323.dkr.ecr.us-east-1.amazonaws.com/petclinic-staging-ecr-mumford"
```

**Verification:** ✅ ECR repository and lifecycle policy created successfully

## ECR Repository Details

**Command:** `aws ecr describe-repositories --repository-names petclinic-staging-ecr-mumford --region us-east-1`

**Output:**
```json
{
    "repositories": [
        {
            "repositoryArn": "arn:aws:ecr:us-east-1:277802554323:repository/petclinic-staging-ecr-mumford",
            "registryId": "277802554323",
            "repositoryName": "petclinic-staging-ecr-mumford",
            "repositoryUri": "277802554323.dkr.ecr.us-east-1.amazonaws.com/petclinic-staging-ecr-mumford",
            "createdAt": "2026-02-26T14:27:44.733000-08:00",
            "imageTagMutability": "MUTABLE",
            "imageScanningConfiguration": {
                "scanOnPush": true
            },
            "encryptionConfiguration": {
                "encryptionType": "AES256"
            }
        }
    ]
}
```

**Key Configuration:**
- Repository Name: `petclinic-staging-ecr-mumford`
- Repository URI: `277802554323.dkr.ecr.us-east-1.amazonaws.com/petclinic-staging-ecr-mumford`
- Image scanning: Enabled (scan on push)
- Encryption: AES256
- Tag mutability: MUTABLE

**Verification:** ✅ ECR repository properly configured with security features

## ECR Lifecycle Policy

**Lifecycle Rules:**
1. **Rule 1**: Remove untagged images older than 1 day
2. **Rule 2**: Keep only last 5 tagged images (prefixes: `latest`, `v`)

**Verification:** ✅ Lifecycle policy configured to manage image retention

## Docker Authentication and Push

**Authentication Command:**
```bash
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 277802554323.dkr.ecr.us-east-1.amazonaws.com
```

**Output:** `Login Succeeded`

**Tag and Push Commands:**
```bash
docker tag petclinic:latest 277802554323.dkr.ecr.us-east-1.amazonaws.com/petclinic-staging-ecr-mumford:latest
docker push 277802554323.dkr.ecr.us-east-1.amazonaws.com/petclinic-staging-ecr-mumford:latest
```

**Push Output:**
```
166a458d67c1: Pushed
8a364b3a6064: Pushed
66a4bbbfab88: Pushed
a793a244b109: Pushed
eb16d5821ee3: Pushed
5485aba22484: Pushed
224a255e1703: Pushed
5ba3b8f27fc4: Pushed
2b1ce970ddfb: Pushed
f599a8b3a683: Pushed
c1f84b78f309: Pushed
latest: digest: sha256:db37b0d099a4a0df029dda11f2f834b77ada83c8b2b9e0e161970239cd1b32f8 size: 856
```

**Verification:** ✅ Docker image successfully pushed to ECR

## ECR Image Verification

**Command:** `aws ecr describe-images --repository-name petclinic-staging-ecr-mumford --region us-east-1`

**Output:**
```json
{
    "imageDetails": [
        {
            "registryId": "277802554323",
            "repositoryName": "petclinic-staging-ecr-mumford",
            "imageDigest": "sha256:db37b0d099a4a0df029dda11f2f834b77ada83c8b2b9e0e161970239cd1b32f8",
            "imageTags": [
                "latest"
            ],
            "imageSizeInBytes": 332060181,
            "imagePushedAt": "2026-02-26T14:31:24.132000-08:00",
            "imageManifestMediaType": "application/vnd.oci.image.index.v1+json",
            "imageStatus": "ACTIVE"
        }
    ]
}
```

**Key Details:**
- Image Digest: `sha256:db37b0d099a4a0df029dda11f2f834b77ada83c8b2b9e0e161970239cd1b32f8`
- Image Tag: `latest`
- Image Size: 332,060,181 bytes (~332 MB)
- Image Status: ACTIVE
- Pushed At: 2026-02-26 14:31:24

**Verification:** ✅ Image successfully stored in ECR and available for deployment

## Files Created

1. **Dockerfile** (repository root) - Multi-stage container build configuration
2. **.dockerignore** (repository root) - Build context exclusions
3. **terraform/staging/ecr.tf** - ECR repository and lifecycle policy
4. **terraform/staging/variables.tf** (updated) - Added `ecr_repository_name` variable

## Infrastructure Summary

**Resources Created:**
- 1 ECR repository (`petclinic-staging-ecr-mumford`)
- 1 ECR lifecycle policy (2 rules)

**Terraform Outputs Added:**
- `ecr_repository_url`: For referencing in ECS task definitions
- `ecr_repository_arn`: For IAM policies and monitoring

## Cost Estimate

**ECR Costs:**
- Storage: $0.10/GB per month (~$0.03/month for 332 MB image)
- Data transfer: First 1 GB free per month from ECR to ECS
- Image scanning: First 30,000 scans per month free

**Total Monthly Cost:** < $0.10 (negligible for staging)

## Security Considerations

✅ **Image Security:**
- Scan on push enabled for vulnerability detection
- Multi-stage build minimizes attack surface
- Non-root user in container
- AES256 encryption at rest

✅ **Access Control:**
- Repository accessible only via IAM roles
- Image tags mutable for development workflow
- Lifecycle policy prevents unbounded storage growth

## Next Steps

With the container image successfully built and stored in ECR, we can now proceed to:
1. Task 2.0: Create ECS cluster and task definition referencing this ECR image
2. Configure ECS tasks to pull from `277802554323.dkr.ecr.us-east-1.amazonaws.com/petclinic-staging-ecr-mumford:latest`

**Task 1.0 Status:** ✅ COMPLETE

All proof artifacts demonstrate successful containerization and ECR repository setup. The Pet Clinic application is ready for deployment to ECS Fargate.
