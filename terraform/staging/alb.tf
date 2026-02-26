# Application Load Balancer Configuration
# Provisions internet-facing ALB, target group for Fargate IP targets, and HTTP listener

# Application Load Balancer
resource "aws_lb" "main" {
  name               = "petclinic-${var.environment}-alb-mumford"
  load_balancer_type = "application"
  internal           = false
  security_groups    = [aws_security_group.alb.id]
  subnets            = [aws_subnet.public.id, aws_subnet.private_2.id]

  enable_deletion_protection = false
  enable_http2               = true
  idle_timeout               = 60

  tags = {
    Name        = "petclinic-${var.environment}-alb-mumford"
    Environment = var.environment
    Project     = "petclinic"
  }
}

# Target Group for ECS Fargate tasks
resource "aws_lb_target_group" "app" {
  name        = "petclinic-${var.environment}-tg-mumford"
  port        = var.ecs_container_port
  protocol    = "HTTP"
  vpc_id      = aws_vpc.main.id
  target_type = "ip"

  health_check {
    enabled             = true
    healthy_threshold   = 2
    unhealthy_threshold = 3
    timeout             = 5
    interval            = 30
    path                = "/actuator/health"
    protocol            = "HTTP"
    port                = "traffic-port"
    matcher             = "200"
  }

  deregistration_delay = 30

  tags = {
    Name        = "petclinic-${var.environment}-tg-mumford"
    Environment = var.environment
  }
}

# HTTP Listener
resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.main.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.app.arn
  }
}
