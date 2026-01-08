CREATE DATABASE IF NOT EXISTS `finsight`;
USE `finsight`;

-- Institutions (MACU, AFCU, Venmo, etc.)
DROP TABLE IF EXISTS `institutions`;
CREATE TABLE `institutions` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(128) NOT NULL,                 -- "MACU"
  `type` VARCHAR(32) DEFAULT NULL,              -- "bank", "credit_union", "wallet"
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_institution_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Accounts (checking/savings/card/venmo wallet, etc.)
DROP TABLE IF EXISTS `accounts`;
CREATE TABLE `accounts` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `institution_id` BIGINT NOT NULL,
  `account_name` VARCHAR(128) NOT NULL,         -- "MACU Checking"
  `account_type` VARCHAR(32) DEFAULT NULL,      -- "checking", "savings", "credit_card"
  `currency` CHAR(3) NOT NULL DEFAULT 'USD',
  `external_account_id` VARCHAR(128) DEFAULT NULL, -- masked/token/OFX id etc.
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_accounts_institution` (`institution_id`),
  UNIQUE KEY `uk_institution_external_acct` (`institution_id`, `external_account_id`),
  CONSTRAINT `fk_accounts_institution`
    FOREIGN KEY (`institution_id`) REFERENCES `institutions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
