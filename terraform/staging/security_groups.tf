# Security Groups
# Defines network access controls for ALB, application, and RDS resources

# Application Load Balancer Security Group
resource "aws_security_group" "alb" {
  name        = "petclinic-${var.environment}-alb-sg-mumford"
  description = "Security group for Application Load Balancer"
  vpc_id      = aws_vpc.main.id

  # Allow HTTP traffic from internet
  ingress {
    description = "HTTP from internet"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Allow HTTPS traffic from internet
  ingress {
    description = "HTTPS from internet"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Allow all outbound traffic
  egress {
    description = "Allow all outbound traffic"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "petclinic-${var.environment}-alb-sg-mumford"
  }
}

# Application Security Group
resource "aws_security_group" "app" {
  name        = "petclinic-${var.environment}-app-sg-mumford"
  description = "Security group for application instances"
  vpc_id      = aws_vpc.main.id

  # Allow traffic from ALB on port 8080 (Spring Boot default)
  ingress {
    description     = "HTTP from ALB"
    from_port       = 8080
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [aws_security_group.alb.id]
  }

  # Allow all outbound traffic
  egress {
    description = "Allow all outbound traffic"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "petclinic-${var.environment}-app-sg-mumford"
  }
}

# RDS Security Group
resource "aws_security_group" "rds" {
  name        = "petclinic-${var.environment}-rds-sg-mumford"
  description = "Security group for RDS PostgreSQL database"
  vpc_id      = aws_vpc.main.id

  # Allow PostgreSQL traffic from application security group
  ingress {
    description     = "PostgreSQL from application"
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.app.id]
  }

  # Allow all outbound traffic
  egress {
    description = "Allow all outbound traffic"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "petclinic-${var.environment}-rds-sg-mumford"
  }
}
