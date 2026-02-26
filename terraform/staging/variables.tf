# Terraform Variables
# Input variable definitions with descriptions and default values

variable "environment" {
  description = "Environment name (staging or production)"
  type        = string
  default     = "staging"
}

variable "vpc_cidr" {
  description = "CIDR block for the VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "public_subnet_cidr" {
  description = "CIDR block for the public subnet (for ALB and NAT Gateway)"
  type        = string
  default     = "10.0.1.0/24"
}

variable "private_subnet_cidr" {
  description = "CIDR block for the private subnet (for application and RDS)"
  type        = string
  default     = "10.0.2.0/24"
}

variable "availability_zone" {
  description = "Availability zone for resources (single AZ for cost optimization)"
  type        = string
  default     = "us-east-1a"
}

variable "backup_retention_days" {
  description = "Number of days to retain automated RDS backups"
  type        = number
  default     = 1
}

variable "ecr_repository_name" {
  description = "Name of the ECR repository for container images"
  type        = string
  default     = "petclinic-staging-ecr-mumford"
}

variable "ecs_task_cpu" {
  description = "CPU units for ECS task (1 vCPU = 1024)"
  type        = number
  default     = 512
}

variable "ecs_task_memory" {
  description = "Memory for ECS task in MB"
  type        = number
  default     = 1024
}

variable "ecs_container_port" {
  description = "Port on which the application container listens"
  type        = number
  default     = 8080
}
