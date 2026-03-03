provider "aws" {
  region = var.aws_region
}

data "aws_caller_identity" "current" {}

# GitHub Actions OIDC Provider
# Allows GitHub Actions to authenticate to AWS without static credentials.
resource "aws_iam_openid_connect_provider" "github" {
  url = "https://token.actions.githubusercontent.com"

  client_id_list = ["sts.amazonaws.com"]

  # AWS validates the OIDC thumbprint against its own CA store for
  # token.actions.githubusercontent.com, but the field is still required.
  thumbprint_list = [
    "6938fd4d98bab03faadb97b34396831e3780aea1",
    "1c58a3a8518e8759bf075b76b750d4f2df264fcd",
  ]

  tags = {
    Name = "github-actions-oidc"
  }
}

# Trust policy: only tokens from this specific repo can assume the role
data "aws_iam_policy_document" "github_actions_assume_role" {
  statement {
    actions = ["sts:AssumeRoleWithWebIdentity"]

    principals {
      type        = "Federated"
      identifiers = [aws_iam_openid_connect_provider.github.arn]
    }

    condition {
      test     = "StringEquals"
      variable = "token.actions.githubusercontent.com:aud"
      values   = ["sts.amazonaws.com"]
    }

    condition {
      test     = "StringLike"
      variable = "token.actions.githubusercontent.com:sub"
      values   = ["repo:${var.github_org}/${var.github_repo}:*"]
    }
  }
}

resource "aws_iam_role" "github_actions" {
  name               = "github-actions-petclinic-mumford"
  assume_role_policy = data.aws_iam_policy_document.github_actions_assume_role.json

  tags = {
    Name = "github-actions-petclinic-mumford"
  }
}

# ── AWS Managed Policies ─────────────────────────────────────────────────────

# ECS + ECR basic auth + Application Auto Scaling
resource "aws_iam_role_policy_attachment" "ecs_full" {
  role       = aws_iam_role.github_actions.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonECS_FullAccess"
}

# ECR image push/pull
resource "aws_iam_role_policy_attachment" "ecr_full" {
  role       = aws_iam_role.github_actions.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryFullAccess"
}

# VPC, subnets, security groups, IGW, NAT Gateway, EIPs, route tables
resource "aws_iam_role_policy_attachment" "ec2_full" {
  role       = aws_iam_role.github_actions.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2FullAccess"
}

# RDS
resource "aws_iam_role_policy_attachment" "rds_full" {
  role       = aws_iam_role.github_actions.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonRDSFullAccess"
}

# Application Load Balancer
resource "aws_iam_role_policy_attachment" "elb_full" {
  role       = aws_iam_role.github_actions.name
  policy_arn = "arn:aws:iam::aws:policy/ElasticLoadBalancingFullAccess"
}

# Secrets Manager (create/manage secrets via Terraform, read at runtime)
resource "aws_iam_role_policy_attachment" "secrets_full" {
  role       = aws_iam_role.github_actions.name
  policy_arn = "arn:aws:iam::aws:policy/SecretsManagerReadWrite"
}

# CloudWatch Logs (Terraform creates log groups for ECS)
resource "aws_iam_role_policy_attachment" "logs_full" {
  role       = aws_iam_role.github_actions.name
  policy_arn = "arn:aws:iam::aws:policy/CloudWatchLogsFullAccess"
}

# IAM — required so Terraform can create/manage ECS task roles and policies
resource "aws_iam_role_policy_attachment" "iam_full" {
  role       = aws_iam_role.github_actions.name
  policy_arn = "arn:aws:iam::aws:policy/IAMFullAccess"
}

# ── Inline Policy: Terraform Remote State ────────────────────────────────────

resource "aws_iam_role_policy" "terraform_state" {
  name = "terraform-state-access"
  role = aws_iam_role.github_actions.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "s3:GetObject",
          "s3:PutObject",
          "s3:DeleteObject",
          "s3:ListBucket",
          "s3:GetBucketVersioning",
        ]
        Resource = [
          "arn:aws:s3:::petclinic-terraform-state-mumford",
          "arn:aws:s3:::petclinic-terraform-state-mumford/*",
        ]
      },
      {
        Effect = "Allow"
        Action = [
          "dynamodb:GetItem",
          "dynamodb:PutItem",
          "dynamodb:DeleteItem",
        ]
        Resource = "arn:aws:dynamodb:${var.aws_region}:${data.aws_caller_identity.current.account_id}:table/petclinic-terraform-locks-mumford"
      },
    ]
  })
}
