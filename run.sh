#!/bin/bash

# ============================================
# Test & Run Cryptocurrency Trading App
# ============================================
# This script builds and launches the app

echo "========================================="
echo "  CRYPTOCURRENCY TRADING APP"
echo "  Build & Run"
echo "========================================="
echo ""

PROJECT_DIR="/home/g701943/Trading_wolfers"
cd "$PROJECT_DIR"

# Setup Maven in PATH
export PATH="/tmp/apache-maven-3.9.8/bin:$PATH"

# Verify Maven exists
if ! command -v mvn &> /dev/null; then
    echo "✗ Maven not found"
    echo "   Run: bash quick_setup.sh"
    exit 1
fi

echo "[1/2] Building application..."
mvn clean package -q -DskipTests

if [ $? -ne 0 ]; then
    echo "✗ Build failed"
    echo "   Check for errors above"
    exit 1
fi

echo "✓ Build successful"
echo ""

echo "[2/2] Launching application..."
echo ""
echo "==========================================="
echo "  APP LAUNCHED"
echo "==========================================="
echo ""
echo "Test Credentials:"
echo "  Email: admin@test.com"
echo "  Password: password123"
echo ""
echo "Or create a new account with Register"
echo ""
echo "Watch this terminal for debug output"
echo "==========================================="
echo ""

mvn javafx:run

