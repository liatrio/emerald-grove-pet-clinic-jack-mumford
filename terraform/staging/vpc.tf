# VPC and Network Infrastructure
# Creates VPC, subnets, Internet Gateway, NAT Gateway, and route tables

# VPC
resource "aws_vpc" "main" {
  cidr_block           = var.vpc_cidr
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = {
    Name = "petclinic-${var.environment}-vpc-mumford"
  }
}

# Public Subnet (for ALB and NAT Gateway)
resource "aws_subnet" "public" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = var.public_subnet_cidr
  availability_zone       = var.availability_zone
  map_public_ip_on_launch = true

  tags = {
    Name = "petclinic-${var.environment}-public-subnet-mumford"
    Type = "public"
  }
}

# Private Subnet 1 (for application and RDS)
resource "aws_subnet" "private" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = var.private_subnet_cidr
  availability_zone = var.availability_zone

  tags = {
    Name = "petclinic-${var.environment}-private-subnet-1-mumford"
    Type = "private"
  }
}

# Private Subnet 2 (for RDS multi-AZ support)
resource "aws_subnet" "private_2" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = "10.0.3.0/24"
  availability_zone = "us-east-1b"

  tags = {
    Name = "petclinic-${var.environment}-private-subnet-2-mumford"
    Type = "private"
  }
}

# Internet Gateway (for public subnet internet access)
resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id

  tags = {
    Name = "petclinic-${var.environment}-igw-mumford"
  }
}

# Elastic IP for NAT Gateway
resource "aws_eip" "nat" {
  domain = "vpc"

  tags = {
    Name = "petclinic-${var.environment}-nat-eip-mumford"
  }

  depends_on = [aws_internet_gateway.main]
}

# NAT Gateway (for private subnet outbound internet access)
resource "aws_nat_gateway" "main" {
  allocation_id = aws_eip.nat.id
  subnet_id     = aws_subnet.public.id

  tags = {
    Name = "petclinic-${var.environment}-nat-mumford"
  }

  depends_on = [aws_internet_gateway.main]
}

# Public Route Table (routes traffic to Internet Gateway)
resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main.id
  }

  tags = {
    Name = "petclinic-${var.environment}-public-rt-mumford"
    Type = "public"
  }
}

# Private Route Table (routes traffic to NAT Gateway)
resource "aws_route_table" "private" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.main.id
  }

  tags = {
    Name = "petclinic-${var.environment}-private-rt-mumford"
    Type = "private"
  }
}

# Route Table Associations
resource "aws_route_table_association" "public" {
  subnet_id      = aws_subnet.public.id
  route_table_id = aws_route_table.public.id
}

resource "aws_route_table_association" "private" {
  subnet_id      = aws_subnet.private.id
  route_table_id = aws_route_table.private.id
}

resource "aws_route_table_association" "private_2" {
  subnet_id      = aws_subnet.private_2.id
  route_table_id = aws_route_table.private.id
}
