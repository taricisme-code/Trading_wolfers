#!/bin/bash

# ============================================
# Run App Locally with XAMPP MySQL (no Docker)
# ============================================

set -e

PROJECT_DIR="/home/g701943/Trading_wolfers"
cd "$PROJECT_DIR"

echo "========================================="
echo "  CRYPTOCURRENCY TRADING APP"
echo "  Local Run (XAMPP / phpMyAdmin)"
echo "========================================="
echo ""

echo "Prereqs:"
echo "  1) Start XAMPP and ensure MySQL is running"
echo "  2) Open phpMyAdmin -> Import -> choose database.sql -> Go"
echo "  3) DB user=root, password is empty (default)"
echo ""

# Setup Maven in PATH - check multiple locations
if [ -d "$HOME/.local/maven/apache-maven-3.9.8/bin" ]; then
  export PATH="$HOME/.local/maven/apache-maven-3.9.8/bin:$PATH"
elif [ -d "/tmp/apache-maven-3.9.8/bin" ]; then
  export PATH="/tmp/apache-maven-3.9.8/bin:$PATH"
fi

if ! command -v mvn &> /dev/null; then
  echo "Maven not found. Installing..."
  bash install_maven.sh
  export PATH="$HOME/.local/maven/apache-maven-3.9.8/bin:$PATH"
  if ! command -v mvn &> /dev/null; then
    echo "✗ Maven installation failed. Install manually or check install_maven.sh"
    exit 1
  fi
fi

mvn -q -v >/dev/null

echo "Building..."
mvn clean package -q -DskipTests

echo "Launching..."
mvn javafx:run
