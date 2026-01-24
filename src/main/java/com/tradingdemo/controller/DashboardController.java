package com.tradingdemo.controller;

import java.io.IOException;

import com.tradingdemo.model.User;
import com.tradingdemo.service.AuthService;
import com.tradingdemo.service.WalletService;
import com.tradingdemo.util.AlertUtils;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * DashboardController - Main dashboard after login
 * Displays user information and navigation to other modules
 */
public class DashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label balanceLabel;
    @FXML private Label portfolioLabel;
    @FXML private Button tradingButton;
    @FXML private Button walletButton;
    @FXML private Button historyButton;
    @FXML private Button newsButton;
    @FXML private Button logoutButton;
    @FXML private VBox adminBox;
    @FXML private Button adminButton;
    @FXML private VBox tradingBox;
    @FXML private VBox walletBox;
    @FXML private VBox historyBox;
    @FXML private VBox newsBox;

    private final WalletService walletService = new WalletService();

    @FXML
    public void initialize() {
        try {
            System.out.println("DEBUG: Initializing DashboardController...");
            
            User currentUser = AuthService.getCurrentUser();
            System.out.println("DEBUG: Current user: " + (currentUser != null ? currentUser.getEmail() : "NULL"));
            
            if (currentUser != null) {
                System.out.println("DEBUG: Setting welcome label...");
                welcomeLabel.setText("Welcome, " + currentUser.getFirstName() + " " + currentUser.getLastName());
                
                // Check if user is admin
                if (currentUser.isAdmin()) {
                    System.out.println("DEBUG: User is admin - hiding user pages, showing admin panel only");
                    
                    // Hide user navigation boxes
                    tradingBox.setVisible(false);
                    tradingBox.setManaged(false);
                    walletBox.setVisible(false);
                    walletBox.setManaged(false);
                    historyBox.setVisible(false);
                    historyBox.setManaged(false);
                    newsBox.setVisible(false);
                    newsBox.setManaged(false);
                    portfolioLabel.setVisible(false);
                    balanceLabel.setVisible(false);
                    
                    // Show admin panel button
                    adminBox.setVisible(true);
                    adminBox.setManaged(true);
                    adminButton.setOnAction(e -> navigateToAdmin());
                    
                    logoutButton.setOnAction(e -> handleLogout());
                    System.out.println("DEBUG: Admin dashboard initialized successfully");
                } else {
                    System.out.println("DEBUG: User is regular user - showing user pages");
                    
                    System.out.println("DEBUG: Updating balance for user ID: " + currentUser.getId());
                    updateBalance(currentUser.getId());
                    
                    System.out.println("DEBUG: Setting up button handlers...");
                    tradingButton.setOnAction(e -> navigateToTrading());
                    walletButton.setOnAction(e -> navigateToWallet());
                    historyButton.setOnAction(e -> navigateToHistory());
                    newsButton.setOnAction(e -> navigateToNews());
                    logoutButton.setOnAction(e -> handleLogout());
                    
                    // Show user navigation boxes
                    tradingBox.setVisible(true);
                    tradingBox.setManaged(true);
                    walletBox.setVisible(true);
                    walletBox.setManaged(true);
                    historyBox.setVisible(true);
                    historyBox.setManaged(true);
                    newsBox.setVisible(true);
                    newsBox.setManaged(true);
                    
                    // Hide admin panel button for regular users
                    adminBox.setVisible(false);
                    adminBox.setManaged(false);
                    
                    System.out.println("DEBUG: User dashboard initialized successfully");
                }
                
            } else {
                System.err.println("ERROR: No user logged in!");
                AlertUtils.showError("Error", "No user logged in");
            }
        } catch (Exception e) {
            System.err.println("ERROR initializing dashboard: " + e.getMessage());
            e.printStackTrace();
            AlertUtils.showError("Dashboard Error", "Error initializing dashboard: " + e.getMessage());
        }
    }

    private void updateBalance(int userId) {
        try {
            System.out.println("DEBUG: Getting balance for user ID: " + userId);
            double balance = walletService.getUserBalance(userId);
            System.out.println("DEBUG: User balance: " + balance);
            balanceLabel.setText(String.format("Balance: $%.2f", balance));
            portfolioLabel.setText("Portfolio: $0.00");
        } catch (Exception e) {
            System.err.println("ERROR updating balance: " + e.getMessage());
            e.printStackTrace();
            balanceLabel.setText("Balance: Error");
        }
    }

    @FXML
    private void navigateToTrading() {
        loadView("/com/tradingdemo/view/trading.fxml", "Trading");
    }

    @FXML
    private void navigateToWallet() {
        loadView("/com/tradingdemo/view/wallet.fxml", "Wallet");
    }

    @FXML
    private void navigateToHistory() {
        loadView("/com/tradingdemo/view/history.fxml", "History");
    }

    @FXML
    private void navigateToNews() {
        loadView("/com/tradingdemo/view/news.fxml", "News");
    }

    @FXML
    private void navigateToAdmin() {
        loadView("/com/tradingdemo/view/admin.fxml", "Admin Panel");
    }

    @FXML
    private void handleLogout() {
        AuthService authService = new AuthService();
        authService.logout();
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tradingdemo/view/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Scene scene = new Scene(root);
            
            // Apply stylesheet
            try {
                String css = getClass().getResource("/com/tradingdemo/view/styles.css").toExternalForm();
                scene.getStylesheets().add(css);
            } catch (Exception ex) {
                System.err.println("Warning: Could not load stylesheet");
            }
            
            stage.setScene(scene);
        } catch (IOException e) {
            System.err.println("ERROR loading login: " + e.getMessage());
            e.printStackTrace();
            AlertUtils.showError("Navigation Error", "Could not load login screen");
        }
    }

    private void loadView(String fxmlPath, String viewName) {
        try {
            System.out.println("DEBUG: Loading view: " + viewName + " from " + fxmlPath);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) tradingButton.getScene().getWindow();
            
            // Create scene that inherits stage dimensions
            Scene scene = new Scene(root);
            
            // Apply stylesheet
            try {
                String css = getClass().getResource("/com/tradingdemo/view/styles.css").toExternalForm();
                scene.getStylesheets().add(css);
            } catch (Exception ex) {
                System.err.println("Warning: Could not load stylesheet: " + ex.getMessage());
            }
            
            stage.setScene(scene);
            System.out.println("DEBUG: Successfully loaded " + viewName);
        } catch (IOException e) {
            System.err.println("ERROR loading " + viewName + ": " + e.getMessage());
            e.printStackTrace();
            AlertUtils.showError("Navigation Error", "Could not load " + viewName + ": " + e.getMessage());
        } catch (Exception e) {
            System.err.println("ERROR: Unexpected exception loading " + viewName + ": " + e.getMessage());
            e.printStackTrace();
            AlertUtils.showError("Navigation Error", "Unexpected error: " + e.getMessage());
        }
    }
}
