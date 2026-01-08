CREATE USER IF NOT EXISTS 'finsight_runtime'@'localhost' IDENTIFIED BY 'f1n31ght_runt1m3!';

GRANT
  SELECT, INSERT, UPDATE, DELETE
ON finsight.* TO 'finsight_runtime'@'localhost';

FLUSH PRIVILEGES;
