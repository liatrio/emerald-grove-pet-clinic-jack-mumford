# RDS PostgreSQL Database
# Provisions managed PostgreSQL database with automated backups and encryption

# Generate random password for database master user
resource "random_password" "db_password" {
  length  = 16
  special = true
  # Avoid characters that might cause issues in connection strings
  override_special = "!#$%&*()-_=+[]{}<>:?"
}

# DB Subnet Group (required for RDS - must span at least 2 AZs)
resource "aws_db_subnet_group" "main" {
  name       = "petclinic-${var.environment}-db-subnet-group-mumford"
  subnet_ids = [aws_subnet.private.id, aws_subnet.private_2.id]

  tags = {
    Name = "petclinic-${var.environment}-db-subnet-group-mumford"
  }
}

# RDS PostgreSQL Instance
resource "aws_db_instance" "postgres" {
  identifier = "petclinic-${var.environment}-db-mumford"

  # Database Configuration
  engine               = "postgres"
  engine_version       = "16.3"
  instance_class       = "db.t3.micro"
  db_name              = "petclinic"
  username             = "petclinic"
  password             = random_password.db_password.result
  parameter_group_name = "default.postgres16"

  # Storage Configuration
  allocated_storage     = 20
  max_allocated_storage = 100
  storage_type          = "gp3"
  storage_encrypted     = true

  # Network Configuration
  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [aws_security_group.rds.id]
  publicly_accessible    = false
  availability_zone      = var.availability_zone

  # Backup Configuration
  backup_retention_period   = var.backup_retention_days
  backup_window             = "03:00-04:00"
  maintenance_window        = "mon:04:00-mon:05:00"
  skip_final_snapshot       = var.environment == "staging" ? true : false
  final_snapshot_identifier = var.environment == "staging" ? null : "petclinic-${var.environment}-final-snapshot-mumford"

  # High Availability (single AZ for cost optimization initially)
  multi_az = false

  # Monitoring
  enabled_cloudwatch_logs_exports = ["postgresql", "upgrade"]

  tags = {
    Name = "petclinic-${var.environment}-db-mumford"
  }
}
