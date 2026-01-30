#!/bin/bash

# Install Maven locally (no sudo, no Docker)
# Downloads Maven to project directory

set -e

MAVEN_VERSION="3.9.8"
MAVEN_DIR="$HOME/.local/maven"
MAVEN_ARCHIVE="apache-maven-${MAVEN_VERSION}-bin.tar.gz"
MAVEN_URL="https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/${MAVEN_ARCHIVE}"

echo "========================================="
echo "  Installing Maven ${MAVEN_VERSION}"
echo "========================================="
echo ""

# Check if already installed
if [ -d "$MAVEN_DIR/apache-maven-${MAVEN_VERSION}" ]; then
    echo "✓ Maven already installed at $MAVEN_DIR/apache-maven-${MAVEN_VERSION}"
    echo ""
    echo "Add to your PATH (add this to ~/.bashrc or ~/.zshrc):"
    echo "  export PATH=\"$MAVEN_DIR/apache-maven-${MAVEN_VERSION}/bin:\$PATH\""
    echo ""
    echo "Or run with full path:"
    echo "  $MAVEN_DIR/apache-maven-${MAVEN_VERSION}/bin/mvn --version"
    exit 0
fi

# Create directory
mkdir -p "$MAVEN_DIR"
cd "$MAVEN_DIR"

echo "[1/3] Downloading Maven..."
wget -q --show-progress "$MAVEN_URL" || {
    echo "✗ Download failed. Check internet connection."
    exit 1
}

echo "[2/3] Extracting..."
tar -xzf "$MAVEN_ARCHIVE"
rm "$MAVEN_ARCHIVE"

echo "[3/3] Setting up..."
MAVEN_HOME="$MAVEN_DIR/apache-maven-${MAVEN_VERSION}"

echo ""
echo "========================================="
echo "  ✓ Maven installed successfully!"
echo "========================================="
echo ""
echo "Maven location: $MAVEN_HOME"
echo ""
echo "To use Maven permanently, add to ~/.bashrc or ~/.zshrc:"
echo "  export PATH=\"$MAVEN_HOME/bin:\$PATH\""
echo ""
echo "Or run this now for current session:"
echo "  export PATH=\"$MAVEN_HOME/bin:\$PATH\""
echo ""
echo "Test it:"
echo "  mvn --version"
echo ""
