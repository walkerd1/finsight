DROP TABLE IF EXISTS `import_profiles`;
CREATE TABLE `import_profiles` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,

  `institution_id` BIGINT DEFAULT NULL,         -- can be NULL for generic profiles
  `name` VARCHAR(128) NOT NULL,                 -- "MACU CSV v1"
  
  -- How we recognize this format again
  `header_signature` CHAR(64) NOT NULL,         -- SHA-256 of normalized headers
  `sample_headers_json` JSON NOT NULL,          -- ["Transaction ID","Posting Date",...]
  
  -- Mapping config that drives parsing:
  -- e.g. {"posted_date":{"column":"Posting Date","format":"MM/DD/YYYY"}, ...}
  `mapping_json` JSON NOT NULL,

  -- Optional: per-profile transformations or rules
  -- e.g. {"amount_sign":"as_is","timezone":"America/Denver"}
  `rules_json` JSON DEFAULT NULL,

  `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (`id`),

  UNIQUE KEY `uk_profile_signature` (`header_signature`),
  KEY `idx_profile_institution` (`institution_id`),

  CONSTRAINT `fk_profiles_institution`
    FOREIGN KEY (`institution_id`) REFERENCES `institutions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
