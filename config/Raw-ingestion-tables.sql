DROP TABLE IF EXISTS `raw_import_files`;
CREATE TABLE `raw_import_files` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,

  `account_id` BIGINT NOT NULL,
  `import_profile_id` BIGINT DEFAULT NULL,

  `original_filename` VARCHAR(255) DEFAULT NULL,
  `content_hash` CHAR(64) NOT NULL,             -- SHA-256 of file bytes (dedupe file imports)
  `row_count` INT DEFAULT NULL,

  `headers_json` JSON NOT NULL,                 -- header array exactly as read
  `imported_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (`id`),

  UNIQUE KEY `uk_file_hash_per_account` (`account_id`, `content_hash`),
  KEY `idx_file_account` (`account_id`),
  KEY `idx_file_profile` (`import_profile_id`),

  CONSTRAINT `fk_raw_files_account`
    FOREIGN KEY (`account_id`) REFERENCES `accounts` (`id`),

  CONSTRAINT `fk_raw_files_profile`
    FOREIGN KEY (`import_profile_id`) REFERENCES `import_profiles` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


DROP TABLE IF EXISTS `raw_transactions`;
CREATE TABLE `raw_transactions` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,

  `raw_file_id` BIGINT NOT NULL,
  `row_number` INT NOT NULL,                    -- line number in file after header

  `row_json` JSON NOT NULL,                     -- {"Transaction ID":"...","Posting Date":"..."}
  `row_hash` CHAR(64) NOT NULL,                 -- SHA-256 fingerprint (dedupe at row level)

  `ingested_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (`id`),

  UNIQUE KEY `uk_row_hash_per_file` (`raw_file_id`, `row_hash`),
  KEY `idx_raw_row_file` (`raw_file_id`),

  CONSTRAINT `fk_raw_tx_file`
    FOREIGN KEY (`raw_file_id`) REFERENCES `raw_import_files` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
