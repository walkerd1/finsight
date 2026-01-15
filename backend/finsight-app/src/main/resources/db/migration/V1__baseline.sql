-- V1 baseline: create all application tables from scratch
-- Flyway will create/manage flyway_schema_history automatically.

CREATE TABLE `institutions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(128) NOT NULL,
  `type` varchar(32) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_institution_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `accounts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `institution_id` bigint NOT NULL,
  `account_name` varchar(128) NOT NULL,
  `account_type` varchar(32) DEFAULT NULL,
  `currency` char(3) NOT NULL DEFAULT 'USD',
  `external_account_id` varchar(128) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_institution_external_acct` (`institution_id`,`external_account_id`),
  KEY `idx_accounts_institution` (`institution_id`),
  CONSTRAINT `fk_accounts_institution`
    FOREIGN KEY (`institution_id`) REFERENCES `institutions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `import_profiles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `institution_id` bigint DEFAULT NULL,
  `name` varchar(128) NOT NULL,
  `header_signature` char(64) NOT NULL,
  `sample_headers_json` json NOT NULL,
  `mapping_json` json NOT NULL,
  `rules_json` json DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_profile_signature` (`header_signature`),
  KEY `idx_profile_institution` (`institution_id`),
  CONSTRAINT `fk_profiles_institution`
    FOREIGN KEY (`institution_id`) REFERENCES `institutions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `raw_import_files` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `account_id` bigint NOT NULL,
  `import_profile_id` bigint DEFAULT NULL,
  `original_filename` varchar(255) DEFAULT NULL,
  `content_hash` char(64) DEFAULT NULL,
  `row_count` int DEFAULT NULL,
  `headers_json` json NOT NULL,
  `status` enum('RECEIVED','INGESTING','FINALIZED','FAILED','DUPLICATE') NOT NULL DEFAULT 'RECEIVED',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `finalized_at` timestamp NULL DEFAULT NULL,
  `failed_at` timestamp NULL DEFAULT NULL,
  `failure_reason` json DEFAULT NULL,
  `finalized_content_hash` char(64)
    GENERATED ALWAYS AS ((case when (`status` = _utf8mb4'FINALIZED') then `content_hash` else NULL end)) STORED,
  PRIMARY KEY (`id`),

  -- NOTE: You previously had UNIQUE(account_id, content_hash) too.
  -- Keeping it OUT aligns with your design: dedupe enforced only at FINALIZED.
  UNIQUE KEY `uq_raw_import_files_account_finalized_hash` (`account_id`,`finalized_content_hash`),

  KEY `idx_file_account` (`account_id`),
  KEY `idx_file_profile` (`import_profile_id`),
  KEY `ix_raw_import_files_account_status_created` (`account_id`,`status`,`created_at`),
  KEY `ix_raw_import_files_account_profile_created` (`account_id`,`import_profile_id`,`created_at`),

  CONSTRAINT `fk_raw_files_account`
    FOREIGN KEY (`account_id`) REFERENCES `accounts` (`id`),
  CONSTRAINT `fk_raw_files_profile`
    FOREIGN KEY (`import_profile_id`) REFERENCES `import_profiles` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `raw_transactions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `raw_file_id` bigint NOT NULL,

  -- corrected: avoid MySQL keyword funk
  `csv_row_number` int NOT NULL,

  `row_json` json NOT NULL,
  `row_hash` char(64) NOT NULL,
  `ingested_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_row_hash_per_file` (`raw_file_id`,`row_hash`),
  KEY `idx_raw_row_file` (`raw_file_id`),
  CONSTRAINT `fk_raw_tx_file`
    FOREIGN KEY (`raw_file_id`) REFERENCES `raw_import_files` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `transactions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `account_id` bigint NOT NULL,
  `raw_transaction_id` bigint DEFAULT NULL,
  `external_transaction_id` varchar(128) DEFAULT NULL,
  `posted_date` date DEFAULT NULL,
  `effective_date` date DEFAULT NULL,
  `amount` decimal(13,5) NOT NULL,
  `currency` char(3) NOT NULL DEFAULT 'USD',
  `direction` enum('DEBIT','CREDIT','UNKNOWN') NOT NULL DEFAULT 'UNKNOWN',
  `description` varchar(1024) DEFAULT NULL,
  `memo` varchar(1024) DEFAULT NULL,
  `running_balance` decimal(13,5) DEFAULT NULL,
  `merchant_name` varchar(255) DEFAULT NULL,
  `category` varchar(128) DEFAULT NULL,
  `payment_channel` varchar(32) DEFAULT NULL,
  `fingerprint_hash` char(64) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tx_fingerprint_per_account` (`account_id`,`fingerprint_hash`),
  KEY `idx_tx_account_posted` (`account_id`,`posted_date`),
  KEY `idx_tx_external_id` (`external_transaction_id`),
  KEY `idx_tx_amount` (`amount`),
  KEY `fk_tx_raw` (`raw_transaction_id`),
  CONSTRAINT `fk_tx_account`
    FOREIGN KEY (`account_id`) REFERENCES `accounts` (`id`),
  CONSTRAINT `fk_tx_raw`
    FOREIGN KEY (`raw_transaction_id`) REFERENCES `raw_transactions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
