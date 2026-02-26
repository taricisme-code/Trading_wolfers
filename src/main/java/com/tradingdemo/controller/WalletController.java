package com.tradingdemo.controller;

import java.io.IOException;
import java.util.List;

import com.tradingdemo.model.WalletItem;
import com.tradingdemo.service.AuthService;
import com.tradingdemo.service.WalletService;
import com.tradingdemo.util.AlertUtils;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * WalletController - Displays user's cryptocurrency holdings
 */
public class WalletController {

    @FXML private ListView<String> walletListView;
    @FXML private Label totalValueLabel;
    @FXML private Button refreshButton;
    @FXML private Button backButton;

    private final WalletService walletService = new WalletService();
    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        // Set custom cell factory for black text
        walletListView.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle(null);
                    } else {
                        setText(item);
                        setStyle("-fx-text-fill: #111827; -fx-font-size: 13px;");
                    }
                }
            };
            return cell;
        });
        
        refreshButton.setOnAction(e -> refreshWallet());
        backButton.setOnAction(e -> goBack());
        refreshWallet();
    }

    private void refreshWallet() {
        walletListView.getItems().clear();
        int userId = authService.getCurrentUser().getId();
        List<WalletItem> items = walletService.getUserWallet(userId);
        
        double totalValue = 0;
        for (WalletItem item : items) {
            // Simulate current prices
            double currentPrice = simulatePrice(item.getSymbol());
            double itemValue = item.getQuantity() * currentPrice;
            totalValue += itemValue;
            
            walletListView.getItems().add(String.format("%s: %.8f @ $%.2f = $%.2f", 
                item.getSymbol(), item.getQuantity(), currentPrice, itemValue));
        }
        
        double balance = walletService.getUserBalance(userId);
        totalValue += balance;
        totalValueLabel.setText(String.format("Total Balance: $%.2f | Cash: $%.2f", totalValue, balance));
    }

    private double simulatePrice(String symbol) {
        // Simulated prices for demo
        return switch(symbol) {
            case "BTC" -> 45000 + (Math.random() * 2000);
            case "ETH" -> 2500 + (Math.random() * 200);
            case "BNB" -> 350 + (Math.random() * 50);
            case "ADA" -> 0.50 + (Math.random() * 0.1);
            case "SOL" -> 140 + (Math.random() * 20);
            case "XRP" -> 2.50 + (Math.random() * 0.5);
            default -> 100;
        };
    }

    @FXML
    private void goBack() {
        loadView("/com/tradingdemo/view/dashboard.fxml");
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root, 1200, 700);
            stage.setScene(scene);
        } catch (IOException e) {
            AlertUtils.showError("Navigation Error", "Could not load view");
        }
    }
}
