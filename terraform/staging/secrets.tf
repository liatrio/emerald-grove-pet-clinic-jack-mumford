# AWS Secrets Manager
# Stores database credentials and API keys securely for application access

# Reference to existing Claude API key secret in Secrets Manager
data "aws_secretsmanager_secret" "claude_api_key" {
  name = "CLAUDE_API_KEY_MUMFORD"
}

# Secrets Manager Secret
resource "aws_secretsmanager_secret" "db_credentials" {
  name                    = "petclinic/${var.environment}/database"
  description             = "Database credentials for Pet Clinic ${var.environment} environment"
  recovery_window_in_days = 0

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
