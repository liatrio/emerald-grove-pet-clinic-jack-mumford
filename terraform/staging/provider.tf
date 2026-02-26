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
