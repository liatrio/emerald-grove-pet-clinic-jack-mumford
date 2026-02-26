# AWS Provider Configuration
# Configures the AWS provider with region and default tags

provider "aws" {
  region = "us-east-1"

  default_tags {
    tags = {
      Project     = "emerald-grove-pet-clinic"
      Environment = var.environment
      ManagedBy   = "terraform"
      Owner       = "mumford"
    }
  }
}

# Data source for current AWS region
data "aws_region" "current" {}

# Data source for current AWS account
data "aws_caller_identity" "current" {}
