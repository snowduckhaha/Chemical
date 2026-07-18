-- Persist P0 lead scoring so inquiry and analytics views share one authoritative value.
SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='inquiry' AND column_name='lead_score')=0, 'ALTER TABLE inquiry ADD COLUMN lead_score INT NOT NULL DEFAULT 0 AFTER first_contacted_at', 'SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='inquiry' AND column_name='lead_level')=0, 'ALTER TABLE inquiry ADD COLUMN lead_level VARCHAR(16) NOT NULL DEFAULT ''LOW'' AFTER lead_score', 'SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE inquiry
SET lead_score =
      (CASE WHEN NULLIF(TRIM(company), '') IS NOT NULL THEN 10 ELSE 0 END) +
      (CASE WHEN NULLIF(TRIM(phone), '') IS NOT NULL THEN 10 ELSE 0 END) +
      (CASE WHEN NULLIF(TRIM(interested_product), '') IS NOT NULL THEN 15 ELSE 0 END) +
      (CASE WHEN CHAR_LENGTH(message) >= 30 THEN 10 ELSE 0 END) +
      (CASE WHEN source_page LIKE '%/products/%' THEN 15 ELSE 0 END),
    lead_level = CASE
      WHEN ((CASE WHEN NULLIF(TRIM(company), '') IS NOT NULL THEN 10 ELSE 0 END) +
            (CASE WHEN NULLIF(TRIM(phone), '') IS NOT NULL THEN 10 ELSE 0 END) +
            (CASE WHEN NULLIF(TRIM(interested_product), '') IS NOT NULL THEN 15 ELSE 0 END) +
            (CASE WHEN CHAR_LENGTH(message) >= 30 THEN 10 ELSE 0 END) +
            (CASE WHEN source_page LIKE '%/products/%' THEN 15 ELSE 0 END)) >= 40 THEN 'HIGH'
      WHEN ((CASE WHEN NULLIF(TRIM(company), '') IS NOT NULL THEN 10 ELSE 0 END) +
            (CASE WHEN NULLIF(TRIM(phone), '') IS NOT NULL THEN 10 ELSE 0 END) +
            (CASE WHEN NULLIF(TRIM(interested_product), '') IS NOT NULL THEN 15 ELSE 0 END)) >= 20 THEN 'MEDIUM'
      ELSE 'LOW' END;
