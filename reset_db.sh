#!/bin/bash

# ============================================
# Reset Database & Test
# ============================================
# This script resets the database and provides
# a fresh start

echo "========================================="
echo "  RESET DATABASE & TEST"
echo "========================================="
echo ""

# Check if database container is running
if ! docker ps | grep -q "trading_db"; then
    echo "✗ Database container not running"
    echo "   Starting container..."
    docker start trading_db
    sleep 5
fi

echo "[1] Dropping old database..."
docker exec trading_db mariadb -u root -proot123 -e "DROP DATABASE IF EXISTS crypto_trading_db;" 2>/dev/null
echo "✓ Old database dropped"
echo ""

echo "[2] Reimporting fresh schema..."
docker exec -i trading_db mariadb -u root -proot123 << 'EOF'
CREATE DATABASE crypto_trading_db;
USE crypto_trading_db;
EOF

docker exec -i trading_db mariadb -u root -proot123 crypto_trading_db < /home/g701943/Trading_wolfers/database_setup.sql
echo "✓ Fresh schema imported"
echo ""

echo "[3] Verifying database..."
docker exec trading_db mariadb -u root -proot123 -D crypto_trading_db -e "SELECT COUNT(*) as table_count FROM information_schema.TABLES WHERE TABLE_SCHEMA='crypto_trading_db';"
echo ""

echo "[4] Listing default users..."
docker exec trading_db mariadb -u root -proot123 -D crypto_trading_db -e "SELECT id, first_name, last_name, email FROM users;"
echo ""

echo "========================================="
echo "  DATABASE RESET COMPLETE ✓"
echo "========================================="
echo ""
echo "Default Test Credentials:"
echo "  Email: admin@test.com"
echo "  Password: password123"
echo ""
echo "NEXT: Build and run the app"
echo "  $ cd /home/g701943/Trading_wolfers"
echo "  $ mvn clean package"
echo "  $ mvn javafx:run"
echo ""
echo "Then try:"
echo "  1. Login with admin@test.com / password123"
echo "  2. Or create a new account with Register"
echo ""
