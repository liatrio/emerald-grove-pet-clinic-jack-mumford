# 04 Questions Round 1 - Infrastructure Restoration

Please answer each question below (select one or more options, or add your own notes). Feel free to add additional context under any question.

## 1. Scope of Terraform Bug Fixes

The context lists 4 known bugs. Looking at the current codebase, Bug 4 (`use_lockfile` in `github-oidc/backend.tf`) appears already fixed. Which bugs still need to be addressed?

- [x] (A) All 4 bugs need to be fixed (BUG 4 may be fixed already but re-verify during implementation)
- [ ] (B) Only Bugs 1, 2, and 3 need to be fixed (BUG 4 is already fixed as `use_lockfile = true` is present in `terraform/github-oidc/backend.tf`)
- [ ] (C) Other (describe)

## 2. ECR Name Conflict Resolution (Bug 1)

Bug 1 is the ECR repository name mismatch: Terraform creates `petclinic-staging-ecr-mumford` but workflows reference `petclinic-staging-repo-mumford`. What is the preferred fix?

- [x] (A) Rename the ECR resource in `ecr.tf` to `petclinic-staging-repo-mumford` (fix Terraform to match workflows — workflows are correct)
- [ ] (B) Update all workflow files to reference `petclinic-staging-ecr-mumford` (fix workflows to match Terraform)
- [ ] (C) Other (describe)

## 3. Duplicate Output Fix Scope (Bug 2)

Bug 2 is duplicate output definitions for `ecr_repository_url` and `ecr_repository_arn`. Currently `ecr.tf` defines these outputs but `outputs.tf` does not appear to have them. How should this be resolved?

- [x] (A) Remove the output blocks from `ecr.tf` only (keep them in `outputs.tf` if they exist, otherwise they can live only in `ecr.tf` — just remove the duplicate)
- [ ] (B) Move the output blocks from `ecr.tf` into `outputs.tf` and remove from `ecr.tf` (centralise all outputs in one file)
- [ ] (C) Other (describe)

## 4. Operational Steps in Scope

This spec involves both code changes (Terraform bug fixes) and operational steps (running Terraform, triggering workflows). What should be in scope?

- [ ] (A) Code changes only — the spec covers only the Terraform file fixes; the operational steps (`terraform apply`, triggering workflows) are described as "how to execute" but are not tracked as spec requirements
- [x] (B) Full end-to-end — the spec covers both the code fixes AND the operational execution steps to restore infrastructure and verify the deployment pipeline works
- [ ] (C) Other (describe)

## 5. Proof Artifacts / Done Criteria

How will we know this spec is complete? What proof is required?

- [ ] (A) Terraform `init` and `plan` succeed locally (no errors from the bug fixes); GitHub Actions deploy workflow succeeds end-to-end at least once
- [ ] (B) Terraform bug fixes only — proof is `terraform validate` or `terraform plan` output showing no errors; no need to actually run the init workflow
- [x] (C) Full end-to-end: OIDC Terraform applied, GitHub secret updated, init-infrastructure workflow run successfully, deploy workflow triggered and passes, application accessible at ALB DNS
- [ ] (D) Other (describe)

## 6. Workflows — Additional Fixes Needed?

Beyond the 4 Terraform bugs, do any of the GitHub Actions workflow files need changes to work correctly with the current setup?

- [ ] (A) No — the workflow files are already correct (they reference `petclinic-staging-repo-mumford` and use OIDC via `AWS_ROLE_ARN` — once the ECR name is fixed in Terraform, everything should align)
- [ ] (B) Yes — there are additional workflow issues (please describe below)
- [ ] (C) Not sure — include a review step in the spec to verify workflow correctness before execution
- [x] (D) Other (describe) I want all of the workflow files to be rewritten. Make them as minimal as possible.
