#!/bin/bash

# ============================================
# Cryptocurrency Trading App - Diagnostics
# ============================================
# This script helps identify login/database issues

echo "========================================="
echo "  CRYPTOCURRENCY TRADING APP"
echo "  Diagnostics & Troubleshooting"
echo "========================================="
echo ""

PROJECT_DIR="/home/g701943/Trading_wolfers"
cd "$PROJECT_DIR"

# Check if Docker container is running
echo "[1] Checking Database Container..."
if docker ps | grep -q "trading_db"; then
    echo "✓ Database container is running"
    
    # Test connection to database
    echo "    Testing database connection..."
    docker exec trading_db mariadb -u root -proot123 -e "SELECT 1;" > /dev/null 2>&1
    if [ $? -eq 0 ]; then
        echo "✓ Database connection works"
    else
        echo "✗ Cannot connect to database"
        exit 1
    fi
else
    echo "✗ Database container NOT running"
    echo "   Run: docker start trading_db"
    echo "   Or: bash quick_setup.sh"
    exit 1
fi

echo ""

# Check if database exists
echo "[2] Checking Database Schema..."
TABLES=$(docker exec trading_db mariadb -u root -proot123 -D crypto_trading_db -e "SHOW TABLES;" 2>/dev/null | wc -l)
if [ $TABLES -gt 1 ]; then
    echo "✓ Database tables exist ($((TABLES-1)) tables found)"
else
    echo "✗ Database tables not found"
    echo "   Reimporting schema..."
    docker exec -i trading_db mariadb -u root -proot123 crypto_trading_db < database_setup.sql
    echo "✓ Schema imported"
fi

echo ""

# Check users in database
echo "[3] Checking Users in Database..."
USER_COUNT=$(docker exec trading_db mariadb -u root -proot123 -D crypto_trading_db -e "SELECT COUNT(*) as count FROM users;" 2>/dev/null | tail -1)
echo "    Users in database: $USER_COUNT"

# List all users
echo "    User details:"
docker exec trading_db mariadb -u root -proot123 -D crypto_trading_db -e "SELECT id, first_name, last_name, email, balance, created_at FROM users;" 2>/dev/null | grep -v "first_name" || echo "    (no users found)"

echo ""

# Check if Maven is available
echo "[4] Checking Build Tools..."
if command -v mvn &> /dev/null; then
    echo "✓ Maven found: $(mvn -v 2>/dev/null | head -1)"
else
    MAVEN_HOME="/tmp/apache-maven-3.9.8"
    if [ -d "$MAVEN_HOME" ]; then
        export PATH="${MAVEN_HOME}/bin:$PATH"
        echo "✓ Maven found (added to PATH)"
    else
        echo "✗ Maven not found. Run: bash quick_setup.sh"
        exit 1
    fi
fi

echo ""

# Check Java
echo "[5] Checking Java Installation..."
if command -v java &> /dev/null; then
    echo "✓ Java found: $(java -version 2>&1 | head -1)"
else
    echo "✗ Java not found"
    exit 1
fi

echo ""

# Rebuild and test
echo "[6] Rebuilding Application..."
echo "    Running: mvn clean package..."
mvn clean package -q -DskipTests 2>&1 | tail -5

if [ $? -eq 0 ]; then
    echo "✓ Build successful"
else
    echo "✗ Build failed"
    echo "    Try: mvn clean package"
    exit 1
fi

echo ""
echo "========================================="
echo "  DIAGNOSTICS COMPLETE ✓"
echo "========================================="
echo ""
echo "NEXT STEPS:"
echo "  1. Run the application: mvn javafx:run"
echo "  2. Try logging in with existing user"
echo "  3. Or create a new account and test login"
echo ""
echo "TROUBLESHOOTING:"
echo ""
echo "If login still fails:"
echo "  • Check the console output for error messages"
echo "  • Verify password is correct (case-sensitive)"
echo "  • Try account with known credentials:"
echo "    - Email: admin@test.com"
echo "    - Password: password123"
echo ""
echo "If that doesn't work:"
echo "  • Database may need fresh data"
echo "  • Run: docker stop trading_db && docker rm trading_db"
echo "  • Then: bash quick_setup.sh"
echo ""

