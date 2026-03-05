# 04 Task 1.0 Proof Artifacts — Fix Terraform Bugs in `ecr.tf`

**Task:** 1.0 Fix Terraform Bugs in `ecr.tf`
**Spec:** 04-spec-infrastructure-restoration
**Date:** 2026-03-03

---

## Changes Made

### Bug 1 — ECR Repository Name Corrected (`terraform/staging/ecr.tf`)

Changed `name` from `petclinic-staging-ecr-mumford` to `petclinic-staging-repo-mumford` to match the name referenced in all three workflow files. Also updated the matching `tags.Name` value.

### Bug 2 — Duplicate Output Blocks Removed (`terraform/staging/ecr.tf`)

Removed the `output "ecr_repository_url"` and `output "ecr_repository_arn"` blocks that existed only in `ecr.tf` (not in `outputs.tf`). These were the source of the duplicate-output error that would cause `terraform init` to fail.

### Bug 3 — `force_delete = true` Added (`terraform/staging/ecr.tf`)

Added `force_delete = true` to the `aws_ecr_repository` resource so the repository can be destroyed via `terraform destroy` even when Docker images are present.

### Bug 4 — `use_lockfile` Verified (`terraform/github-oidc/backend.tf`)

Confirmed `terraform/github-oidc/backend.tf` already contains `use_lockfile = true` and does not contain the deprecated `dynamodb_table` parameter. No change required.

---

## CLI Output — `terraform validate` in `terraform/staging/`

```
Success! The configuration is valid, but there were some
validation warnings as shown above.
```

> Note: The only warnings are local provider development overrides (`hashicorp.com/edu/hashicups`) which are pre-existing on this machine and unrelated to the changes made.

---

## CLI Output — `terraform validate` in `terraform/github-oidc/`

```
Success! The configuration is valid, but there were some
validation warnings as shown above.
```

> Same local provider override warning. No errors.

---

## File Diff — `terraform/staging/ecr.tf`

```diff
resource "aws_ecr_repository" "petclinic" {
-  name                 = "petclinic-staging-ecr-mumford"
+  name                 = "petclinic-staging-repo-mumford"
   image_tag_mutability = "MUTABLE"
+  force_delete         = true

   image_scanning_configuration {
     scan_on_push = true
   }

   encryption_configuration {
     encryption_type = "AES256"
   }

   tags = {
-    Name = "petclinic-staging-ecr-mumford"
+    Name = "petclinic-staging-repo-mumford"
   }
 }

 # ECR Lifecycle Policy
 ...

-# Output ECR repository URI for use in task definitions
-output "ecr_repository_url" {
-  description = "URL of the ECR repository for Pet Clinic application"
-  value       = aws_ecr_repository.petclinic.repository_url
-}
-
-output "ecr_repository_arn" {
-  description = "ARN of the ECR repository"
-  value       = aws_ecr_repository.petclinic.arn
-}
```

---

## Verification

| Bug | Fix | Status |
|-----|-----|--------|
| Bug 1: ECR name mismatch | Renamed to `petclinic-staging-repo-mumford` in `ecr.tf` | ✅ Fixed |
| Bug 2: Duplicate outputs | Removed `ecr_repository_url` and `ecr_repository_arn` from `ecr.tf` | ✅ Fixed |
| Bug 3: Missing `force_delete` | Added `force_delete = true` to `aws_ecr_repository` resource | ✅ Fixed |
| Bug 4: Deprecated `dynamodb_table` | Verified `use_lockfile = true` already present in `github-oidc/backend.tf` | ✅ Confirmed |
| `terraform validate` staging | Returns `Success!` | ✅ Passed |
| `terraform validate` github-oidc | Returns `Success!` | ✅ Passed |
