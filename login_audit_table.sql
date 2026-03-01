-- Login Audit table for recording IP information and login history
CREATE TABLE `login_audit` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL,
  `ip_address` VARCHAR(50) NOT NULL,
  `country` VARCHAR(100),
  `city` VARCHAR(100),
  `region` VARCHAR(100),
  `isp` VARCHAR(100),
  `timezone` VARCHAR(100),
  `login_time` DATETIME NOT NULL,
  `user_agent` VARCHAR(255),
  KEY `idx_audit_user` (`user_id`),
  KEY `idx_audit_time` (`login_time`),
  CONSTRAINT `fk_audit_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;
