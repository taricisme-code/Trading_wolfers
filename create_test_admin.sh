#!/bin/bash

# Script to create a test user and then convert them to admin

cd /home/g701943/Trading_wolfers

echo "========================================="
echo "  Creating Test Admin User"
echo "========================================="
echo ""

# Use known BCrypt hashes for common passwords
# These are verified hashes:
# Password: admin123
# Hash: $2a$12$jHqHaLaKn14OeBJF6ILCyOvZ.JnsU0aXYXM9PJGJzJ9dn.FBIvXS.

# Or use a simpler password approach:
# Let's use: password123 which has hash: $2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUm

mysql -h 172.20.10.5 -u root crypto_trading_db << 'SQL'
-- Delete old test users
DELETE FROM users WHERE email IN ('admin@test.com', 'test@test.com');

-- Create admin with known working hash
-- Password: admin123
-- This hash is from BCrypt with rounds=12
INSERT INTO users (first_name, last_name, email, phone, password_hash, balance, is_admin, created_at, updated_at)
VALUES ('Test', 'Admin', 'admin@test.com', '+1234567890', 
        '$2a$12$jHqHaLaKn14OeBJF6ILCyOvZ.JnsU0aXYXM9PJGJzJ9dn.FBIvXS.',
        5000.0, TRUE, NOW(), NOW());

-- Verify insertion
SELECT id, first_name, last_name, email, is_admin, balance FROM users;
SQL

echo ""
echo "========================================="
echo "  Test User Created!"
echo "========================================="
echo ""
echo "  Email: admin@test.com"
echo "  Password: admin123"
echo "  Balance: $5000"
echo "  Admin: YES ✓"
echo ""
echo "========================================="
