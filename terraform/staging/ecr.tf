# Amazon ECR Repository
# Provides secure Docker image registry for Pet Clinic application containers

resource "aws_ecr_repository" "petclinic" {
  name                 = "petclinic-staging-ecr-mumford"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  encryption_configuration {
    encryption_type = "AES256"
  }

  tags = {
    Name = "petclinic-staging-ecr-mumford"
  }
}

# ECR Lifecycle Policy
# Retains only the last 5 tagged images and removes untagged images older than 1 day
resource "aws_ecr_lifecycle_policy" "petclinic" {
  repository = aws_ecr_repository.petclinic.name

  policy = jsonencode({
    rules = [
      {
        rulePriority = 1
        description  = "Remove untagged images older than 1 day"
        selection = {
          tagStatus   = "untagged"
          countType   = "sinceImagePushed"
          countUnit   = "days"
          countNumber = 1
        }
        action = {
          type = "expire"
        }
      },
      {
        rulePriority = 2
        description  = "Keep only last 5 tagged images"
        selection = {
          tagStatus     = "tagged"
          tagPrefixList = ["latest", "v"]
          countType     = "imageCountMoreThan"
          countNumber   = 5
        }
        action = {
          type = "expire"
        }
      }
    ]
  })
}

# Output ECR repository URI for use in task definitions
output "ecr_repository_url" {
  description = "URL of the ECR repository for Pet Clinic application"
  value       = aws_ecr_repository.petclinic.repository_url
}

output "ecr_repository_arn" {
  description = "ARN of the ECR repository"
  value       = aws_ecr_repository.petclinic.arn
}
