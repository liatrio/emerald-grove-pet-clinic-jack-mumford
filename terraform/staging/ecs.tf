# Amazon ECS Configuration
# Provisions ECS Fargate cluster, task definition, and service for Pet Clinic application

# ECS Cluster
resource "aws_ecs_cluster" "main" {
  name = "petclinic-${var.environment}-cluster-mumford"

  setting {
    name  = "containerInsights"
    value = "enabled"
  }

  tags = {
    Name = "petclinic-${var.environment}-cluster-mumford"
  }
}

# CloudWatch Log Group for ECS tasks
resource "aws_cloudwatch_log_group" "ecs" {
  name              = "/ecs/petclinic-${var.environment}-mumford"
  retention_in_days = 7

  tags = {
    Name = "petclinic-${var.environment}-ecs-logs-mumford"
  }
}

# ECS Task Definition
resource "aws_ecs_task_definition" "petclinic" {
  family                   = "petclinic-${var.environment}-task-mumford"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = var.ecs_task_cpu
  memory                   = var.ecs_task_memory
  execution_role_arn       = aws_iam_role.ecs_task_execution.arn
  task_role_arn            = aws_iam_role.ecs_task.arn

  container_definitions = jsonencode([
    {
      name      = "petclinic"
      image     = "${aws_ecr_repository.petclinic.repository_url}:latest"
      essential = true

      portMappings = [
        {
          containerPort = var.ecs_container_port
          hostPort      = var.ecs_container_port
          protocol      = "tcp"
        }
      ]

      environment = [
        {
          name  = "SPRING_PROFILES_ACTIVE"
          value = "postgres"
        },
        {
          name  = "JAVA_OPTS"
          value = "-Xmx768m -XX:+UseContainerSupport"
        },
        {
          name  = "SPRING_DATASOURCE_URL"
          value = "jdbc:postgresql://${aws_db_instance.postgres.address}:${aws_db_instance.postgres.port}/${aws_db_instance.postgres.db_name}"
        }
      ]

      secrets = [
        {
          name      = "SPRING_DATASOURCE_USERNAME"
          valueFrom = "${aws_secretsmanager_secret.db_credentials.arn}:username::"
        },
        {
          name      = "SPRING_DATASOURCE_PASSWORD"
          valueFrom = "${aws_secretsmanager_secret.db_credentials.arn}:password::"
        }
      ]

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.ecs.name
          "awslogs-region"        = data.aws_region.current.name
          "awslogs-stream-prefix" = "ecs"
        }
      }

      healthCheck = {
        command     = ["CMD-SHELL", "curl -f http://localhost:${var.ecs_container_port}/actuator/health || exit 1"]
        interval    = 30
        timeout     = 5
        retries     = 3
        startPeriod = 60
      }
    },
    {
      name      = "xray-daemon"
      image     = "public.ecr.aws/xray/aws-xray-daemon:latest"
      essential = false
      cpu       = 32
      memory    = 256

      portMappings = [
        {
          containerPort = 2000
          hostPort      = 2000
          protocol      = "udp"
        }
      ]

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.ecs.name
          "awslogs-region"        = data.aws_region.current.name
          "awslogs-stream-prefix" = "xray"
        }
      }
    }
  ])

  tags = {
    Name = "petclinic-${var.environment}-task-mumford"
  }
}
