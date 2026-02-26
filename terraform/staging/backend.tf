# Terraform State Backend Configuration
# This configures remote state storage in S3 with DynamoDB locking
# for team collaboration and state conflict prevention.

terraform {
  backend "s3" {
    bucket         = "petclinic-terraform-state-mumford"
    key            = "staging/terraform.tfstate"
    region         = "us-east-1"
    encrypt        = true
    dynamodb_table = "petclinic-terraform-locks-mumford"
  }
}
