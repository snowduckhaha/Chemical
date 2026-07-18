-- Anonymous conversion analytics storage. No personal inquiry fields are stored here.
CREATE TABLE IF NOT EXISTS analytics_event (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  event_id VARCHAR(64) NOT NULL,
  event_name VARCHAR(64) NOT NULL,
  occurred_at DATETIME(3) NOT NULL,
  visitor_id VARCHAR(64) NOT NULL,
  session_id VARCHAR(64) NOT NULL,
  page_key VARCHAR(255) NOT NULL,
  page_path VARCHAR(500) NOT NULL,
  page_type VARCHAR(64) NULL,
  referrer VARCHAR(500) NULL,
  utm_source VARCHAR(128) NULL,
  utm_medium VARCHAR(128) NULL,
  utm_campaign VARCHAR(255) NULL,
  source_channel VARCHAR(32) NOT NULL,
  lang VARCHAR(8) NULL,
  country VARCHAR(128) NULL,
  category_id BIGINT NULL,
  series_id BIGINT NULL,
  product_id BIGINT NULL,
  payload_json JSON NULL,
  user_agent VARCHAR(500) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_analytics_event_id (event_id),
  KEY idx_analytics_event_time_name (occurred_at, event_name),
  KEY idx_analytics_visitor_session (visitor_id, session_id),
  KEY idx_analytics_page_time (page_key, occurred_at),
  KEY idx_analytics_channel_time (source_channel, occurred_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS analytics_daily_aggregate (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  stat_date DATE NOT NULL,
  dimension_type VARCHAR(32) NOT NULL,
  dimension_key VARCHAR(255) NOT NULL,
  lang VARCHAR(8) NOT NULL DEFAULT '',
  source_channel VARCHAR(32) NOT NULL DEFAULT '',
  metric_name VARCHAR(64) NOT NULL,
  metric_value DECIMAL(20,4) NOT NULL DEFAULT 0,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_analytics_daily (stat_date, dimension_type, dimension_key, lang, source_channel, metric_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
