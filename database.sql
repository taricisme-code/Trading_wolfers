-- Crypto Trading Demo - MySQL schema for local XAMPP
-- Import this in phpMyAdmin (localhost) before running the app

-- Drop and recreate database (optional)
DROP DATABASE IF EXISTS `crypto_trading_db`;
CREATE DATABASE `crypto_trading_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `crypto_trading_db`;

-- Users table
CREATE TABLE `users` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `first_name` VARCHAR(100) NOT NULL,
  `last_name` VARCHAR(100) NOT NULL,
  `email` VARCHAR(190) NOT NULL UNIQUE,
  `phone` VARCHAR(50),
  `password_hash` VARCHAR(255) NOT NULL,
  `balance` DOUBLE NOT NULL DEFAULT 1000.0,
  `is_admin` BOOLEAN NOT NULL DEFAULT FALSE,
  `two_factor_enabled` BOOLEAN NOT NULL DEFAULT FALSE,
  `two_factor_secret` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME NOT NULL,
  `updated_at` DATETIME
) ENGINE=InnoDB;

-- Wallet items (user holdings)
CREATE TABLE `wallet_items` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL,
  `symbol` VARCHAR(20) NOT NULL,
  `quantity` DOUBLE NOT NULL DEFAULT 0,
  `average_price` DOUBLE NOT NULL DEFAULT 0,
  UNIQUE KEY `uniq_user_symbol` (`user_id`,`symbol`),
  KEY `idx_wallet_user` (`user_id`),
  CONSTRAINT `fk_wallet_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- Orders
CREATE TABLE `orders` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL,
  `symbol` VARCHAR(20) NOT NULL,
  `type` VARCHAR(20) NOT NULL,           -- MARKET, LIMIT
  `side` VARCHAR(10) NOT NULL,           -- BUY, SELL
  `price` DOUBLE NOT NULL,
  `quantity` DOUBLE NOT NULL,
  `status` VARCHAR(20) NOT NULL,         -- PENDING, EXECUTED, CANCELLED
  `stop_loss` DOUBLE DEFAULT 0,
  `take_profit` DOUBLE DEFAULT 0,
  `created_at` DATETIME NOT NULL,
  KEY `idx_orders_user` (`user_id`),
  KEY `idx_orders_symbol` (`symbol`),
  CONSTRAINT `fk_orders_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- Trades (executions)
CREATE TABLE `trades` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `order_id` INT NOT NULL,
  `symbol` VARCHAR(20) NOT NULL,
  `side` VARCHAR(10) NOT NULL,          -- BUY, SELL
  `executed_price` DOUBLE NOT NULL,
  `quantity` DOUBLE NOT NULL,
  `executed_at` DATETIME NOT NULL,
  KEY `idx_trades_order` (`order_id`),
  CONSTRAINT `fk_trades_order` FOREIGN KEY (`order_id`) REFERENCES `orders`(`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- News
CREATE TABLE `news` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `title` VARCHAR(255) NOT NULL,
  `content` MEDIUMTEXT NOT NULL,
  `source` VARCHAR(100),
  `published_at` DATETIME NOT NULL
) ENGINE=InnoDB;

-- Predictions
CREATE TABLE `predictions` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `symbol` VARCHAR(20) NOT NULL,
  `signal` VARCHAR(20) NOT NULL,           -- BUY, SELL, HOLD
  `confidence` DOUBLE NOT NULL,
  `target_price` DOUBLE NOT NULL,
  `analysis` MEDIUMTEXT
) ENGINE=InnoDB;

-- Reclamations (support tickets)
CREATE TABLE `reclamations` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL,
  `title` VARCHAR(255) NOT NULL,
  `content` MEDIUMTEXT NOT NULL,
  `status` VARCHAR(50) NOT NULL DEFAULT 'OPEN',
  `created_at` DATETIME NOT NULL,
  `updated_at` DATETIME,
  KEY `idx_recls_user` (`user_id`),
  CONSTRAINT `fk_recls_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- Alerts table for price notifications
CREATE TABLE `alerts` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL,
  `symbol` VARCHAR(20) NOT NULL,
  `target_price` DOUBLE NOT NULL,
  `notify_when_above` BOOLEAN NOT NULL DEFAULT TRUE,
  `enabled` BOOLEAN NOT NULL DEFAULT TRUE,
  `notify_email` BOOLEAN NOT NULL DEFAULT TRUE,
  `notify_sms` BOOLEAN NOT NULL DEFAULT FALSE,
  `created_at` DATETIME NOT NULL,
  KEY `idx_alerts_user` (`user_id`),
  CONSTRAINT `fk_alerts_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- Password reset codes for 'forgot password' flow
CREATE TABLE `password_resets` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL,
  `token` VARCHAR(100) NOT NULL,
  `expires_at` DATETIME NOT NULL,
  `created_at` DATETIME NOT NULL,
  KEY `idx_pr_user` (`user_id`),
  CONSTRAINT `fk_pr_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- Optional seed data (no users pre-seeded to let you register from the app)
-- INSERT INTO users (first_name, last_name, email, phone, password_hash, balance, created_at, updated_at)
-- VALUES ('Admin', 'User', 'admin@test.com', NULL, '$2a$10$abcdefghijklmnopqrstuvC9b0e1b2c3d4e5f6g7h8i9jkl', 1000.0, NOW(), NOW());
