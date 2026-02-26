# Terraform Outputs
# Expose key infrastructure resource identifiers for application deployment and reference

# VPC Outputs
output "vpc_id" {
  description = "ID of the VPC"
  value       = aws_vpc.main.id
}

output "vpc_cidr" {
  description = "CIDR block of the VPC"
  value       = aws_vpc.main.cidr_block
}

output "public_subnet_id" {
  description = "ID of the public subnet (for ALB and NAT Gateway)"
  value       = aws_subnet.public.id
}

output "private_subnet_id" {
  description = "ID of the first private subnet (for application and RDS)"
  value       = aws_subnet.private.id
}

output "private_subnet_2_id" {
  description = "ID of the second private subnet (for RDS multi-AZ support)"
  value       = aws_subnet.private_2.id
}

output "availability_zone" {
  description = "Primary availability zone for resources"
  value       = var.availability_zone
}

# Security Group Outputs
output "alb_security_group_id" {
  description = "ID of the Application Load Balancer security group"
  value       = aws_security_group.alb.id
}

output "app_security_group_id" {
  description = "ID of the application security group"
  value       = aws_security_group.app.id
}

output "rds_security_group_id" {
  description = "ID of the RDS PostgreSQL security group"
  value       = aws_security_group.rds.id
}

# RDS Outputs
output "rds_endpoint" {
  description = "Connection endpoint for the RDS PostgreSQL database"
  value       = aws_db_instance.postgres.endpoint
}

output "rds_address" {
  description = "Hostname of the RDS PostgreSQL database"
  value       = aws_db_instance.postgres.address
}

output "rds_port" {
  description = "Port number for RDS PostgreSQL database connections"
  value       = aws_db_instance.postgres.port
}

output "rds_database_name" {
  description = "Name of the PostgreSQL database"
  value       = aws_db_instance.postgres.db_name
}

output "rds_username" {
  description = "Master username for database access"
  value       = aws_db_instance.postgres.username
  sensitive   = true
}

output "rds_arn" {
  description = "ARN of the RDS PostgreSQL instance"
  value       = aws_db_instance.postgres.arn
}

# Secrets Manager Outputs
output "secrets_manager_secret_arn" {
  description = "ARN of the Secrets Manager secret containing database credentials"
  value       = aws_secretsmanager_secret.db_credentials.arn
}

output "secrets_manager_secret_name" {
  description = "Name of the Secrets Manager secret for credential retrieval"
  value       = aws_secretsmanager_secret.db_credentials.name
}

# Network Gateway Outputs
output "internet_gateway_id" {
  description = "ID of the Internet Gateway"
  value       = aws_internet_gateway.main.id
}

output "nat_gateway_id" {
  description = "ID of the NAT Gateway"
  value       = aws_nat_gateway.main.id
}

output "nat_gateway_public_ip" {
  description = "Public IP address of the NAT Gateway"
  value       = aws_eip.nat.public_ip
}

# Environment Information
output "environment" {
  description = "Environment name (staging or production)"
  value       = var.environment
}

output "region" {
  description = "AWS region where resources are deployed"
  value       = data.aws_region.current.name
}
