CREATE TABLE IF NOT EXISTS deployment_job (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  environment VARCHAR(16) NOT NULL,
  source_ref VARCHAR(64) NOT NULL DEFAULT 'origin/main',
  requested_by VARCHAR(128) NOT NULL,
  requested_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  status VARCHAR(16) NOT NULL DEFAULT 'QUEUED',
  resolved_commit VARCHAR(64) NULL,
  started_at DATETIME NULL,
  finished_at DATETIME NULL,
  log_excerpt TEXT NULL,
  error_summary VARCHAR(1000) NULL,
  KEY idx_deployment_job_status_created (status, requested_at),
  KEY idx_deployment_job_environment_created (environment, requested_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
