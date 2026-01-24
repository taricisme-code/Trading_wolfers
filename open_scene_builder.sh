#!/bin/bash

# Quick launcher for Scene Builder with project FXML files

FXML_DIR="/home/g701943/project_java_fx/src/main/resources/com/tradingdemo/view"

echo "========================================="
echo "  Scene Builder Quick Launch"
echo "========================================="
echo ""
echo "Available FXML files:"
echo "  1) login.fxml - Login screen"
echo "  2) register.fxml - Registration form"
echo "  3) dashboard.fxml - Main dashboard"
echo "  4) trading.fxml - Trading terminal"
echo "  5) wallet.fxml - Wallet view"
echo "  6) history.fxml - Trade history"
echo "  7) news.fxml - News feed"
echo ""
echo -n "Enter number (1-7) or press Enter to open Scene Builder without file: "
read choice

case $choice in
    1)
        echo "Opening login.fxml..."
        flatpak run com.gluonhq.SceneBuilder "$FXML_DIR/login.fxml"
        ;;
    2)
        echo "Opening register.fxml..."
        flatpak run com.gluonhq.SceneBuilder "$FXML_DIR/register.fxml"
        ;;
    3)
        echo "Opening dashboard.fxml..."
        flatpak run com.gluonhq.SceneBuilder "$FXML_DIR/dashboard.fxml"
        ;;
    4)
        echo "Opening trading.fxml..."
        flatpak run com.gluonhq.SceneBuilder "$FXML_DIR/trading.fxml"
        ;;
    5)
        echo "Opening wallet.fxml..."
        flatpak run com.gluonhq.SceneBuilder "$FXML_DIR/wallet.fxml"
        ;;
    6)
        echo "Opening history.fxml..."
        flatpak run com.gluonhq.SceneBuilder "$FXML_DIR/history.fxml"
        ;;
    7)
        echo "Opening news.fxml..."
        flatpak run com.gluonhq.SceneBuilder "$FXML_DIR/news.fxml"
        ;;
    *)
        echo "Opening Scene Builder..."
        flatpak run com.gluonhq.SceneBuilder
        ;;
esac
