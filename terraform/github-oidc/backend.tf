terraform {
  backend "s3" {
    bucket         = "petclinic-terraform-state-mumford"
    key            = "global/github-oidc/terraform.tfstate"
    region         = "us-east-1"
    encrypt        = true
    use_lockfile   = true
  }
}
