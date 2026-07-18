SET NAMES utf8mb4;

SET @add_deleted_at = (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE application_field ADD COLUMN deleted_at DATETIME NULL',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'application_field'
    AND column_name = 'deleted_at'
);
PREPARE application_field_stmt FROM @add_deleted_at;
EXECUTE application_field_stmt;
DEALLOCATE PREPARE application_field_stmt;

SET @add_application_field_index = (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE application_field ADD KEY idx_application_field_status_sort (publish_status, deleted_at, sort_order)',
    'SELECT 1')
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'application_field'
    AND index_name = 'idx_application_field_status_sort'
);
PREPARE application_field_index_stmt FROM @add_application_field_index;
EXECUTE application_field_index_stmt;
DEALLOCATE PREPARE application_field_index_stmt;

SET @add_application_series_index = (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE application_series_link ADD KEY idx_application_series_link_application_sort (application_id, publish_status, sort_order)',
    'SELECT 1')
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'application_series_link'
    AND index_name = 'idx_application_series_link_application_sort'
);
PREPARE application_series_index_stmt FROM @add_application_series_index;
EXECUTE application_series_index_stmt;
DEALLOCATE PREPARE application_series_index_stmt;
