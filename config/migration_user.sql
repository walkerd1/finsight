CREATE USER IF NOT EXISTS 'finsight_migrate'@'localhost' IDENTIFIED BY 'f1n31ght_m1grat3!';

GRANT
  CREATE, ALTER, DROP, INDEX,
  REFERENCES,
  CREATE VIEW, SHOW VIEW,
  TRIGGER, EVENT
ON finsight.* TO 'finsight_migrate'@'localhost';

FLUSH PRIVILEGES;
