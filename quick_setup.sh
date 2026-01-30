#!/bin/bash

# ============================================
# Cryptocurrency Trading App - Complete Setup
# ============================================
# This is a simplified, all-in-one setup script
# Just run: bash quick_setup.sh

echo "========================================="
echo "  CRYPTOCURRENCY TRADING APP"
echo "  Quick Setup & Run"
echo "========================================="
echo ""

PROJECT_DIR="/home/g701943/project_java_fx"
cd "$PROJECT_DIR"

# ============================================
# STEP 1: Start Docker MariaDB
# ============================================
echo "[1/5] Starting database container..."

CONTAINER_ID=$(docker ps -aq -f "name=trading_db" 2>/dev/null)
if [ -n "$CONTAINER_ID" ]; then
    RUNNING=$(docker ps -q -f "name=trading_db" 2>/dev/null)
    if [ -z "$RUNNING" ]; then
        echo "      Restarting database container..."
        docker start trading_db > /dev/null 2>&1
        sleep 5
    else
        echo "      ✓ Database already running"
    fi
else
    echo "      Creating database container (first time only)..."
    docker run -d \
        --name trading_db \
        -e MYSQL_ROOT_PASSWORD=root123 \
        -e MYSQL_DATABASE=crypto_trading_db \
        -p 3306:3306 \
        mariadb:latest > /dev/null 2>&1
    echo "      Waiting for database to initialize..."
    sleep 15
fi

echo "✓ Database is ready"
echo ""

# ============================================
# STEP 2: Setup Database Schema
# ============================================
echo "[2/5] Setting up database schema..."

# Try to create database and import schema
if command -v mariadb &> /dev/null; then
    mariadb -h 127.0.0.1 -u root -proot123 crypto_trading_db < database_setup.sql 2>/dev/null
    echo "✓ Database schema imported"
else
    echo "      MariaDB client not found, using docker exec..."
    docker exec -i trading_db mariadb -u root -proot123 << EOF
DROP DATABASE IF EXISTS crypto_trading_db;
CREATE DATABASE crypto_trading_db;
EOF
    docker exec -i trading_db mariadb -u root -proot123 crypto_trading_db < database_setup.sql
    echo "✓ Database schema imported"
fi

echo ""

# ============================================
# STEP 3: Configure Database Connection
# ============================================
echo "[3/5] Configuring database credentials..."

CONFIG_FILE="src/main/java/com/tradingdemo/config/DatabaseConnection.java"
sed -i 's/private static final String DB_PASSWORD = "";/private static final String DB_PASSWORD = "root123";/' "$CONFIG_FILE"

echo "✓ Database configuration updated"
echo ""

# ============================================
# STEP 4: Setup Maven
# ============================================
echo "[4/5] Setting up Maven..."

if command -v mvn &> /dev/null; then
    echo "✓ Maven already installed: $(mvn -v 2>/dev/null | head -1)"
else
    echo "      Downloading Maven 3.9.8..."
    MAVEN_HOME="/tmp/apache-maven-3.9.8"
    if [ ! -d "$MAVEN_HOME" ]; then
        mkdir -p /tmp
        cd /tmp
        wget -q https://archive.apache.org/dist/maven/maven-3/3.9.8/binaries/apache-maven-3.9.8-bin.tar.gz
        tar -xzf apache-maven-3.9.8-bin.tar.gz > /dev/null 2>&1
        cd "$PROJECT_DIR"
    fi
    export PATH="${MAVEN_HOME}/bin:$PATH"
    echo "✓ Maven ready"
fi

echo ""

# ============================================
# STEP 5: Build Project
# ============================================
echo "[5/5] Building project (first build takes 2-3 minutes)..."

mvn clean package -q -DskipTests

if [ $? -eq 0 ]; then
    echo "✓ Project built successfully"
else
    echo "✗ Build failed. Try manually:"
    echo "  mvn clean package"
    exit 1
fi

echo ""
echo "========================================="
echo "  ✓ SETUP COMPLETE!"
echo "========================================="
echo ""
echo "Your application is ready to run!"
echo ""
echo "Starting application..."
echo ""

# Run the application
mvn javafx:run

