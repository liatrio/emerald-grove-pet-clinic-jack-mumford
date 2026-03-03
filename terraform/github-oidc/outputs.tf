output "github_actions_role_arn" {
  description = "ARN of the GitHub Actions IAM role — set this as AWS_ROLE_ARN in GitHub Secrets"
  value       = aws_iam_role.github_actions.arn
}

output "oidc_provider_arn" {
  description = "ARN of the GitHub Actions OIDC identity provider"
  value       = aws_iam_openid_connect_provider.github.arn
}
