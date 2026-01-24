-- Insert Admin User for Testing
USE `crypto_trading_db`;

-- Insert admin user (password: admin123)
INSERT INTO `users` (`first_name`, `last_name`, `email`, `phone`, `password_hash`, `balance`, `is_admin`, `created_at`, `updated_at`)
VALUES ('Admin', 'User', 'admin@test.com', '+1234567890', '$2a$12$K.ZSzVqOsO5xO0a5SxP0m.KyL8pGkKj3qcNVblr/7X0a4XMFKD3u6', 5000.0, TRUE, NOW(), NOW());

-- Verify insertion
SELECT id, email, is_admin, balance FROM users WHERE email = 'admin@test.com';
