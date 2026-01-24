#!/bin/bash

# Cryptocurrency Trading App - Complete Setup & Run Script
# This script will:
# 1. Start MariaDB/MySQL in Docker
# 2. Create the database
# 3. Download Maven if needed
# 4. Build the project
# 5. Run the application

set -e

echo "========================================"
echo "  Cryptocurrency Trading App Setup"
echo "========================================"
echo ""

# Step 1: Start MySQL/MariaDB in Docker
echo "Step 1: Starting MariaDB container..."
CONTAINER_NAME="trading_db"
CONTAINER_PORT="3306"

# Check if container is already running
if docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
    echo "✓ Container '$CONTAINER_NAME' is already running"
else
    # Check if container exists but is stopped
    if docker ps -a --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
        echo "  Restarting existing container..."
        docker start "$CONTAINER_NAME"
        sleep 3
    else
        echo "  Creating new MariaDB container..."
        docker run -d \
            --name "$CONTAINER_NAME" \
            -e MYSQL_ROOT_PASSWORD=root123 \
            -e MYSQL_DATABASE=crypto_trading_db \
            -p ${CONTAINER_PORT}:3306 \
            mariadb:latest
        echo "  Waiting for MariaDB to be ready..."
        sleep 10
    fi
fi

echo "✓ MariaDB is running on localhost:3306"
echo ""

# Step 2: Create database and tables
echo "Step 2: Setting up database..."
# Wait a bit more to ensure MySQL is ready
sleep 3

# Import the database schema
mysql -h 127.0.0.1 -u root -proot123 << 'SQLEOF'
DROP DATABASE IF EXISTS crypto_trading_db;
CREATE DATABASE crypto_trading_db;
USE crypto_trading_db;
SQLEOF

# Now import the main schema
mysql -h 127.0.0.1 -u root -proot123 crypto_trading_db < database_setup.sql

echo "✓ Database created and populated"
echo ""

# Step 3: Update DatabaseConnection.java with correct credentials
echo "Step 3: Configuring database connection..."
CONFIG_FILE="src/main/java/com/tradingdemo/config/DatabaseConnection.java"

# Update password to match our Docker setup
sed -i 's/private static final String DB_PASSWORD = "";/private static final String DB_PASSWORD = "root123";/' "$CONFIG_FILE"

# Update host if needed (should be localhost)
if grep -q "localhost" "$CONFIG_FILE"; then
    echo "✓ Database configuration updated"
else
    echo "⚠ Warning: Could not verify database configuration"
fi

echo ""

# Step 4: Check and download Maven if needed
echo "Step 4: Checking Maven installation..."
if ! command -v mvn &> /dev/null; then
    echo "  Maven not found. Downloading..."
    
    # Download Maven
    MAVEN_VERSION="3.9.8"
    MAVEN_HOME="/tmp/apache-maven-${MAVEN_VERSION}"
    
    if [ ! -d "$MAVEN_HOME" ]; then
        mkdir -p /tmp
        cd /tmp
        wget -q "https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz"
        tar -xzf "apache-maven-${MAVEN_VERSION}-bin.tar.gz"
        cd - > /dev/null
    fi
    
    export PATH="${MAVEN_HOME}/bin:$PATH"
    echo "✓ Maven downloaded and added to PATH"
else
    echo "✓ Maven found: $(mvn --version | head -1)"
fi

echo ""

# Step 5: Build the project
echo "Step 5: Building project (this may take 2-3 minutes on first build)..."
mvn clean package -q

echo "✓ Project built successfully"
echo ""

# Step 6: Run the application
echo "Step 6: Launching application..."
echo "========================================"
echo "Application is starting..."
echo "========================================"
echo ""
echo "Test Credentials:"
echo "  Email: admin@test.com"
echo "  Password: password123"
echo ""
echo "or create a new account using 'Register Now'"
echo ""

mvn javafx:run

