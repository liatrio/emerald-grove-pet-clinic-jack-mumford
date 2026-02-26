# AWS Secrets Manager
# Stores database credentials securely for application access

# Secrets Manager Secret
resource "aws_secretsmanager_secret" "db_credentials" {
  name        = "petclinic/${var.environment}/database"
  description = "Database credentials for Pet Clinic ${var.environment} environment"

  tags = {
    Name = "petclinic-${var.environment}-db-credentials-mumford"
  }
}

# Secrets Manager Secret Version (stores actual credentials)
resource "aws_secretsmanager_secret_version" "db_credentials" {
  secret_id = aws_secretsmanager_secret.db_credentials.id
  secret_string = jsonencode({
    host     = aws_db_instance.postgres.address
    port     = aws_db_instance.postgres.port
    username = aws_db_instance.postgres.username
    password = random_password.db_password.result
    dbname   = aws_db_instance.postgres.db_name
    engine   = "postgres"
  })
}
