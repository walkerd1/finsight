DROP TABLE IF EXISTS `transactions`;
CREATE TABLE `transactions` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,

  `account_id` BIGINT NOT NULL,
  `raw_transaction_id` BIGINT DEFAULT NULL,     -- back-reference (traceability)

  -- Canonical core fields
  `external_transaction_id` VARCHAR(128) DEFAULT NULL, -- if present in source
  `posted_date` DATE DEFAULT NULL,
  `effective_date` DATE DEFAULT NULL,
  `amount` DECIMAL(13,5) NOT NULL,
  `currency` CHAR(3) NOT NULL DEFAULT 'USD',

  `direction` ENUM('DEBIT','CREDIT','UNKNOWN') NOT NULL DEFAULT 'UNKNOWN',

  `description` VARCHAR(1024) DEFAULT NULL,
  `memo` VARCHAR(1024) DEFAULT NULL,

  `running_balance` DECIMAL(13,5) DEFAULT NULL,

  -- Optional structured enrichment (fill later)
  `merchant_name` VARCHAR(255) DEFAULT NULL,
  `category` VARCHAR(128) DEFAULT NULL,
  `payment_channel` VARCHAR(32) DEFAULT NULL,  -- Card/POS/ACH/etc

  -- Canonical-level dedupe fingerprint
  `fingerprint_hash` CHAR(64) NOT NULL,

  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (`id`),

  KEY `idx_tx_account_posted` (`account_id`, `posted_date`),
  KEY `idx_tx_external_id` (`external_transaction_id`),
  KEY `idx_tx_amount` (`amount`),

  UNIQUE KEY `uk_tx_fingerprint_per_account` (`account_id`, `fingerprint_hash`),

  CONSTRAINT `fk_tx_account`
    FOREIGN KEY (`account_id`) REFERENCES `accounts` (`id`),

  CONSTRAINT `fk_tx_raw`
    FOREIGN KEY (`raw_transaction_id`) REFERENCES `raw_transactions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
