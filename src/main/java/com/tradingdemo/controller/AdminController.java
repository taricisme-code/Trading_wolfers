package com.tradingdemo.controller;

import java.util.List;

import com.tradingdemo.dao.TradeDAO;
import com.tradingdemo.dao.UserDAO;
import com.tradingdemo.model.Order;
import com.tradingdemo.model.Trade;
import com.tradingdemo.model.User;
import com.tradingdemo.service.AuthService;
import com.tradingdemo.service.TradingService;
import com.tradingdemo.util.AlertUtils;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * AdminController - Manages the admin panel
 * Shows user management, trade history, orders, and system statistics
 */
public class AdminController {
    
    @FXML private Button backButton;
    @FXML private Button logoutButton;
    
    // Users Tab
    @FXML private Label totalUsersLabel;
    @FXML private Label activeTradersLabel;
    @FXML private Label totalBalanceLabel;
    @FXML private TextArea usersLog;
    @FXML private TextField userIdField;
    @FXML private TextField newBalanceField;
    @FXML private Button updateBalanceButton;
    @FXML private Button deleteUserButton;
    @FXML private Button refreshUsersButton;
    
    // Trades Tab
    @FXML private Label totalTradesLabel;
    @FXML private Label totalBuysLabel;
    @FXML private Label totalSellsLabel;
    @FXML private TextArea tradesLog;
    
    // Orders Tab
    @FXML private Label totalOrdersLabel;
    @FXML private Label pendingOrdersLabel;
    @FXML private Label executedOrdersLabel;
    @FXML private TextArea ordersLog;
    
    // System Stats Tab
    @FXML private Label cryptoCountLabel;
    @FXML private Label volumeLabel;
    @FXML private Label avgBalanceLabel;
    @FXML private TextArea activityLog;
    
    private final UserDAO userDAO = new UserDAO();
    private final TradeDAO tradeDAO = new TradeDAO();
    private final TradingService tradingService = new TradingService();
    
    @FXML
    public void initialize() {
        try {
            // Check if user is admin
            User currentUser = AuthService.getCurrentUser();
            if (currentUser == null || !currentUser.isAdmin()) {
                AlertUtils.showError("Access Denied", "You don't have admin privileges!");
                return;
            }
            
            // Setup buttons
            backButton.setOnAction(e -> goBackToDashboard());
            logoutButton.setOnAction(e -> handleLogout());
            
            // User management buttons
            updateBalanceButton.setOnAction(e -> handleUpdateBalance());
            deleteUserButton.setOnAction(e -> handleDeleteUser());
            refreshUsersButton.setOnAction(e -> loadUsersData());
            
            // Load admin data
            loadUsersData();
            loadTradesData();
            loadOrdersData();
            loadSystemStats();
        } catch (Exception e) {
            System.err.println("ERROR in AdminController.initialize(): " + e.getMessage());
            e.printStackTrace();
            AlertUtils.showError("Admin Panel Error", "Error initializing admin panel: " + e.getMessage());
        }
    }
    
    private void handleUpdateBalance() {
        try {
            String userIdText = userIdField.getText().trim();
            String balanceText = newBalanceField.getText().trim();
            
            if (userIdText.isEmpty() || balanceText.isEmpty()) {
                AlertUtils.showError("Input Error", "Please enter both User ID and New Balance");
                return;
            }
            
            int userId = Integer.parseInt(userIdText);
            double newBalance = Double.parseDouble(balanceText.replace("$", "").replace(",", ""));
            
            // Check if user exists
            User user = userDAO.getUserById(userId);
            if (user == null) {
                AlertUtils.showError("User Not Found", "No user found with ID: " + userId);
                return;
            }
            
            // Prevent modifying admin users
            if (user.isAdmin()) {
                AlertUtils.showError("Cannot Modify Admin", "Cannot modify balance of admin users");
                return;
            }
            
            // Update balance
            if (userDAO.updateBalance(userId, newBalance)) {
                AlertUtils.showInfo("Success", String.format("Balance updated for %s %s to $%.2f", 
                    user.getFirstName(), user.getLastName(), newBalance));
                loadUsersData(); // Refresh the users list
                userIdField.clear();
                newBalanceField.clear();
            } else {
                AlertUtils.showError("Update Failed", "Failed to update user balance");
            }
            
        } catch (NumberFormatException e) {
            AlertUtils.showError("Invalid Input", "Please enter valid numbers for User ID and Balance");
        } catch (Exception e) {
            AlertUtils.showError("Error", "Error updating balance: " + e.getMessage());
        }
    }
    
    private void handleDeleteUser() {
        try {
            String userIdText = userIdField.getText().trim();
            
            if (userIdText.isEmpty()) {
                AlertUtils.showError("Input Error", "Please enter a User ID to delete");
                return;
            }
            
            int userId = Integer.parseInt(userIdText);
            
            // Check if user exists
            User user = userDAO.getUserById(userId);
            if (user == null) {
                AlertUtils.showError("User Not Found", "No user found with ID: " + userId);
                return;
            }
            
            // Prevent deleting admin users
            if (user.isAdmin()) {
                AlertUtils.showError("Cannot Delete Admin", "Cannot delete admin users");
                return;
            }
            
            // Prevent deleting yourself
            User currentUser = AuthService.getCurrentUser();
            if (currentUser != null && currentUser.getId() == userId) {
                AlertUtils.showError("Cannot Delete Self", "You cannot delete your own account");
                return;
            }
            
            // Confirm deletion
            boolean confirmed = AlertUtils.showConfirmation("Confirm Deletion", 
                String.format("Are you sure you want to delete user %s %s (%s)?", 
                    user.getFirstName(), user.getLastName(), user.getEmail()));
            
            if (confirmed) {
                if (userDAO.deleteUser(userId)) {
                    AlertUtils.showInfo("Success", "User deleted successfully");
                    loadUsersData(); // Refresh the users list
                    userIdField.clear();
                    newBalanceField.clear();
                } else {
                    AlertUtils.showError("Delete Failed", "Failed to delete user");
                }
            }
            
        } catch (NumberFormatException e) {
            AlertUtils.showError("Invalid Input", "Please enter a valid User ID");
        } catch (Exception e) {
            AlertUtils.showError("Error", "Error deleting user: " + e.getMessage());
        }
    }
    
    private void loadUsersData() {
        try {
            List<User> users = userDAO.getAllUsers();
            
            totalUsersLabel.setText(String.valueOf(users.size()));
            
            // Count active traders (those with orders)
            int activeTraders = 0;
            double totalBalance = 0;
            StringBuilder usersList = new StringBuilder();
            
            for (User user : users) {
                totalBalance += user.getBalance();
                var orders = tradingService.getAllOrdersForUser(user.getId());
                if (orders != null && !orders.isEmpty()) {
                    activeTraders++;
                }
                
                // Format user info for display
                usersList.append(String.format("ID: %d | Email: %s | Name: %s %s | Balance: $%.2f | Admin: %s%n",
                    user.getId(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getBalance(),
                    user.isAdmin() ? "YES" : "NO"));
            }
            
            activeTradersLabel.setText(String.valueOf(activeTraders));
            totalBalanceLabel.setText(String.format("$%.2f", totalBalance));
            usersLog.setText(usersList.toString());
            
        } catch (Exception e) {
            System.err.println("Error loading users: " + e.getMessage());
            e.printStackTrace();
            usersLog.setText("Error loading users: " + e.getMessage());
        }
    }
    
    private void loadTradesData() {
        try {
            List<Trade> trades = tradeDAO.getAllTrades();
            if (trades == null) {
                trades = new java.util.ArrayList<>();
            }
            
            totalTradesLabel.setText(String.valueOf(trades.size()));
            
            // Count buy and sell orders
            int buys = 0;
            int sells = 0;
            StringBuilder tradesList = new StringBuilder();
            
            for (Trade trade : trades) {
                if ("BUY".equals(trade.getSide())) {
                    buys++;
                } else if ("SELL".equals(trade.getSide())) {
                    sells++;
                }
                
                // Format trade info for display
                tradesList.append(String.format("Trade ID: %d | Symbol: %s | Side: %s | Price: $%.2f | Qty: %.4f | Executed: %s%n",
                    trade.getId(),
                    trade.getSymbol(),
                    trade.getSide(),
                    trade.getExecutedPrice(),
                    trade.getQuantity(),
                    trade.getExecutedAt()));
            }
            
            totalBuysLabel.setText(String.valueOf(buys));
            totalSellsLabel.setText(String.valueOf(sells));
            tradesLog.setText(tradesList.toString());
            
        } catch (Exception e) {
            System.err.println("Error loading trades: " + e.getMessage());
            e.printStackTrace();
            tradesLog.setText("Error loading trades: " + e.getMessage());
        }
    }
    
    private void loadOrdersData() {
        try {
            // For simplicity, get all users and their orders
            List<User> users = userDAO.getAllUsers();
            List<Order> allOrders = new java.util.ArrayList<>();
            
            int pending = 0;
            int executed = 0;
            StringBuilder ordersList = new StringBuilder();
            
            for (User user : users) {
                var userOrders = tradingService.getAllOrdersForUser(user.getId());
                for (Order order : userOrders) {
                    allOrders.add(order);
                    if ("PENDING".equals(order.getStatus())) {
                        pending++;
                    } else if ("EXECUTED".equals(order.getStatus())) {
                        executed++;
                    }
                    
                    // Format order info for display
                    ordersList.append(String.format("Order ID: %d | User: %s | Symbol: %s | Type: %s | Side: %s | Price: $%.2f | Qty: %.4f | Status: %s%n",
                        order.getId(),
                        user.getEmail(),
                        order.getSymbol(),
                        order.getType(),
                        order.getSide(),
                        order.getPrice(),
                        order.getQuantity(),
                        order.getStatus()));
                }
            }
            
            totalOrdersLabel.setText(String.valueOf(allOrders.size()));
            pendingOrdersLabel.setText(String.valueOf(pending));
            executedOrdersLabel.setText(String.valueOf(executed));
            ordersLog.setText(ordersList.toString());
            
        } catch (Exception e) {
            System.err.println("Error loading orders: " + e.getMessage());
            e.printStackTrace();
            ordersLog.setText("Error loading orders: " + e.getMessage());
        }
    }
    
    private void loadSystemStats() {
        try {
            List<User> users = userDAO.getAllUsers();
            List<Trade> trades = tradeDAO.getAllTrades();
            
            // Count unique cryptocurrencies
            java.util.Set<String> cryptos = new java.util.HashSet<>();
            if (trades != null) {
                for (Trade trade : trades) {
                    cryptos.add(trade.getSymbol());
                }
            }
            cryptoCountLabel.setText(String.valueOf(cryptos.size()));
            
            // Calculate total volume
            double totalVolume = 0;
            if (trades != null) {
                for (Trade trade : trades) {
                    totalVolume += trade.getExecutedPrice() * trade.getQuantity();
                }
            }
            volumeLabel.setText(String.format("$%.2f", totalVolume));
            
            // Calculate average balance
            double avgBalance = 0;
            if (!users.isEmpty()) {
                double sum = 0;
                for (User user : users) {
                    sum += user.getBalance();
                }
                avgBalance = sum / users.size();
            }
            avgBalanceLabel.setText(String.format("$%.2f", avgBalance));
            
            // Activity log
            String log = "Platform loaded at " + java.time.LocalDateTime.now() + "\n\n";
            log += "Total Users: " + users.size() + "\n";
            log += "Total Trades: " + (trades != null ? trades.size() : 0) + "\n";
            log += "Unique Cryptocurrencies: " + cryptos.size() + "\n";
            log += "Total Platform Volume: $" + String.format("%.2f", totalVolume) + "\n";
            
            activityLog.setText(log);
            
        } catch (Exception e) {
            System.err.println("Error loading system stats: " + e.getMessage());
        }
    }
    
    private void goBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tradingdemo/view/dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root);
            String css = getClass().getResource("/com/tradingdemo/view/styles.css").toExternalForm();
            scene.getStylesheets().add(css);
            stage.setScene(scene);
        } catch (Exception e) {
            System.err.println("Error going back: " + e.getMessage());
        }
    }
    
    private void handleLogout() {
        AuthService.getCurrentUser().setId(0);  // Clear current user
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tradingdemo/view/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Scene scene = new Scene(root);
            String css = getClass().getResource("/com/tradingdemo/view/styles.css").toExternalForm();
            scene.getStylesheets().add(css);
            stage.setScene(scene);
        } catch (Exception e) {
            System.err.println("Error during logout: " + e.getMessage());
        }
    }
}
