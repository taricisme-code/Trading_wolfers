package com.tradingdemo.controller;

import java.io.IOException;
import java.util.Random;

import com.tradingdemo.model.Order;
import com.tradingdemo.service.AuthService;
import com.tradingdemo.service.TradingService;
import com.tradingdemo.service.WalletService;
import com.tradingdemo.util.AlertUtils;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * TradingController - Handles cryptocurrency trading interface with live charts
 * Allows users to place buy/sell orders with stop-loss and take-profit
 */
public class TradingController {

    // Chart and Market Data Components
    @FXML private LineChart<String, Number> priceChart;
    @FXML private Label selectedSymbolLabel;
    @FXML private Label currentPriceLabel;
    @FXML private Label priceChangeLabel;
    @FXML private Label highLabel;
    @FXML private Label lowLabel;
    @FXML private Label volumeLabel;
    @FXML private Label marketCapLabel;
    @FXML private Label supplyLabel;
    @FXML private Label athLabel;
    @FXML private Label balanceHeaderLabel;
    @FXML private Label quantityUnitLabel;
    @FXML private Label availableBalanceLabel;
    
    // Crypto Tab Buttons
    @FXML private Button btcButton;
    @FXML private Button ethButton;
    @FXML private Button bnbButton;
    @FXML private Button adaButton;
    @FXML private Button solButton;
    @FXML private Button xrpButton;
    
    // Trading Form Components
    @FXML private ComboBox<String> symbolCombo;
    @FXML private TextField quantityField;
    @FXML private TextField priceField;
    @FXML private ComboBox<String> orderTypeCombo;
    @FXML private ComboBox<String> sideCombo;
    @FXML private TextField stopLossField;
    @FXML private TextField takeProfitField;
    @FXML private Label totalCostLabel;
    @FXML private Button placeOrderButton;
    @FXML private Button backButton;
    @FXML private ListView<String> orderListView;
    @FXML private ToggleButton buyToggle;
    @FXML private ToggleButton sellToggle;
    @FXML private Button closePositionButton;
    @FXML private Button cancelOrderButton;
    @FXML private Button refreshOrdersButton;
    
    // Store order IDs for the list
    private java.util.List<Order> displayedOrders = new java.util.ArrayList<>();

    private final TradingService tradingService = new TradingService();
    private final WalletService walletService = new WalletService();
    private final AuthService authService = new AuthService();

    private static final String[] CRYPTOCURRENCIES = {"BTC", "ETH", "BNB", "ADA", "SOL", "XRP", "DOGE", "USDC"};
    private static final String[] ORDER_TYPES = {"MARKET", "LIMIT"};
    private static final String[] SIDES = {"BUY", "SELL"};
    private static final double[] BASE_PRICES = {45000, 2500, 350, 0.50, 140, 2.50, 0.08, 1.0};
    private static final String[] CRYPTO_NAMES = {"Bitcoin", "Ethereum", "Binance Coin", "Cardano", "Solana", "Ripple", "Dogecoin", "USD Coin"};
    
    private String currentSymbol = "BTC";
    private double currentPrice = BASE_PRICES[0];
    private Timeline priceUpdateTimeline;
    private Random random = new Random();

    @FXML
    public void initialize() {
        setupComboBoxes();
        setupBuySellToggle();
        setupCryptoTabs();
        setupPriceCalculation();
        placeOrderButton.setOnAction(e -> handlePlaceOrder());
        backButton.setOnAction(e -> goBack());
        
        // Position management buttons
        closePositionButton.setOnAction(e -> handleClosePosition());
        cancelOrderButton.setOnAction(e -> handleCancelOrder());
        refreshOrdersButton.setOnAction(e -> refreshOrderList());
        
        // Initialize chart and price data
        updateUserBalance();
        switchToCrypto("BTC", 0);
        refreshOrderList();
        
        // Start live price updates
        startPriceUpdates();
    }

    private void setupComboBoxes() {
        symbolCombo.getItems().addAll(CRYPTOCURRENCIES);
        symbolCombo.setValue("BTC");
        
        orderTypeCombo.getItems().addAll(ORDER_TYPES);
        orderTypeCombo.setValue("MARKET");  // MARKET orders execute immediately and update balance
        
        sideCombo.getItems().addAll(SIDES);
        sideCombo.setValue("BUY");
    }
    
    private void setupBuySellToggle() {
        ToggleGroup toggleGroup = new ToggleGroup();
        buyToggle.setToggleGroup(toggleGroup);
        sellToggle.setToggleGroup(toggleGroup);
        buyToggle.setSelected(true);
        
        // Update side combo when toggle changes
        buyToggle.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                sideCombo.setValue("BUY");
                placeOrderButton.setText("PLACE BUY ORDER");
                placeOrderButton.setStyle("-fx-padding: 14; -fx-font-size: 14; -fx-font-weight: bold; -fx-background-color: #238636; -fx-text-fill: white; -fx-cursor: hand; -fx-border-radius: 6; -fx-background-radius: 6; -fx-pref-width: 310;");
            }
        });
        
        sellToggle.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                sideCombo.setValue("SELL");
                placeOrderButton.setText("PLACE SELL ORDER");
                placeOrderButton.setStyle("-fx-padding: 14; -fx-font-size: 14; -fx-font-weight: bold; -fx-background-color: #da3633; -fx-text-fill: white; -fx-cursor: hand; -fx-border-radius: 6; -fx-background-radius: 6; -fx-pref-width: 310;");
            }
        });
    }
    
    private void setupCryptoTabs() {
        btcButton.setOnAction(e -> switchToCrypto("BTC", 0));
        ethButton.setOnAction(e -> switchToCrypto("ETH", 1));
        bnbButton.setOnAction(e -> switchToCrypto("BNB", 2));
        adaButton.setOnAction(e -> switchToCrypto("ADA", 3));
        solButton.setOnAction(e -> switchToCrypto("SOL", 4));
        xrpButton.setOnAction(e -> switchToCrypto("XRP", 5));
    }
    
    private void switchToCrypto(String symbol, int index) {
        currentSymbol = symbol;
        currentPrice = BASE_PRICES[index];
        symbolCombo.setValue(symbol);
        
        // Update visual active state
        Button[] buttons = {btcButton, ethButton, bnbButton, adaButton, solButton, xrpButton};
        for (int i = 0; i < buttons.length; i++) {
            if (i == index) {
                buttons[i].setStyle("-fx-padding: 8 15; -fx-background-color: #1f6feb; -fx-text-fill: white; -fx-cursor: hand; -fx-border-radius: 6; -fx-background-radius: 6; -fx-font-weight: bold;");
            } else {
                buttons[i].setStyle("-fx-padding: 8 15; -fx-background-color: #21262d; -fx-text-fill: #c9d1d9; -fx-cursor: hand; -fx-border-radius: 6; -fx-background-radius: 6;");
            }
        }
        
        // Update labels
        selectedSymbolLabel.setText(symbol + "/USD");
        quantityUnitLabel.setText("(" + symbol + ")");
        
        // Update chart
        updatePriceChart();
        updateMarketStats(symbol, index);
    }
    
    private void updatePriceChart() {
        priceChart.getData().clear();
        priceChart.setStyle("-fx-background-color: #0d1117;");
        priceChart.setPrefHeight(400);
        
        // Generate 24 hours of simulated price data
        double price = currentPrice;
        String[] timeLabels = {"00:00", "02:00", "04:00", "06:00", "08:00", "10:00", "12:00", 
                               "14:00", "16:00", "18:00", "20:00", "22:00", "24:00"};
        
        XYChart.Series<String, Number> mainPriceSeries = new XYChart.Series<>();
        mainPriceSeries.setName("Price");
        
        // Store price data for calculations
        double[] prices = new double[timeLabels.length];
        
        for (int i = 0; i < timeLabels.length; i++) {
            price = price * (1 + (random.nextDouble() - 0.5) * 0.05);
            prices[i] = price;
            mainPriceSeries.getData().add(new XYChart.Data<>(timeLabels[i], price));
        }
        
        // Add main price line
        priceChart.getData().add(mainPriceSeries);
        
        // Style main price line - Blue
        priceChart.lookup(".chart-series-line").setStyle("-fx-stroke: #58a6ff; -fx-stroke-width: 2.5px;");
        
        // Add Take Profit and Stop Loss level lines (like TradingView)
        addTPSLLines(timeLabels, prices);
        
        // Add trade markers and order details
        try {
            addTradeMarkersToChart(timeLabels, prices);
        } catch (Exception e) {
            System.err.println("ERROR adding chart markers: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Update current price from last data point
        currentPrice = prices[prices.length - 1];
        updatePriceLabels();
    }
    
    /**
     * Add Take Profit (TP) and Stop Loss (SL) reference lines (TradingView style)
     */
    private void addTPSLLines(String[] timeLabels, double[] prices) {
        // Get all orders for current symbol to show their TP/SL levels
        var orders = tradingService.getAllOrdersForUser(authService.getCurrentUser().getId());
        
        boolean addedTP = false;
        boolean addedSL = false;
        
        // Show TP/SL from placed orders
        for (Order order : orders) {
            if (order.getSymbol().equals(currentSymbol)) {
                // Add Take Profit line if set
                if (order.getTakeProfit() > 0 && !addedTP) {
                    XYChart.Series<String, Number> tpLine = new XYChart.Series<>();
                    tpLine.setName("TP: $" + String.format("%.2f", order.getTakeProfit()));
                    for (String time : timeLabels) {
                        tpLine.getData().add(new XYChart.Data<>(time, order.getTakeProfit()));
                    }
                    priceChart.getData().add(tpLine);
                    addedTP = true;
                    
                    // Style TP line - green dashed
                    javafx.application.Platform.runLater(() -> {
                        var lines = priceChart.lookupAll(".chart-series-line");
                        if (lines.size() > 0) {
                            javafx.scene.Node line = (javafx.scene.Node) lines.toArray()[lines.size() - 1];
                            line.setStyle("-fx-stroke: #56d364; -fx-stroke-width: 2; -fx-stroke-dash-array: 5 5;");
                        }
                    });
                    
                    System.out.println("DEBUG: Added TP line at $" + order.getTakeProfit());
                }
                
                // Add Stop Loss line if set
                if (order.getStopLoss() > 0 && !addedSL) {
                    XYChart.Series<String, Number> slLine = new XYChart.Series<>();
                    slLine.setName("SL: $" + String.format("%.2f", order.getStopLoss()));
                    for (String time : timeLabels) {
                        slLine.getData().add(new XYChart.Data<>(time, order.getStopLoss()));
                    }
                    priceChart.getData().add(slLine);
                    addedSL = true;
                    
                    // Style SL line - red dashed
                    javafx.application.Platform.runLater(() -> {
                        var lines = priceChart.lookupAll(".chart-series-line");
                        if (lines.size() > 0) {
                            javafx.scene.Node line = (javafx.scene.Node) lines.toArray()[lines.size() - 1];
                            line.setStyle("-fx-stroke: #f85149; -fx-stroke-width: 2; -fx-stroke-dash-array: 5 5;");
                        }
                    });
                    
                    System.out.println("DEBUG: Added SL line at $" + order.getStopLoss());
                }
            }
        }
    }

    private void addTradeMarkersToChart(String[] timeLabels, double[] prices) {
        var orders = tradingService.getPendingOrders(authService.getCurrentUser().getId());
        
        // Separate series for buy and sell markers
        XYChart.Series<String, Number> buyMarkers = new XYChart.Series<>();
        buyMarkers.setName("BUY Entry");
        
        XYChart.Series<String, Number> sellMarkers = new XYChart.Series<>();
        sellMarkers.setName("SELL Exit");
        
        // Add order markers
        for (Order order : orders) {
            if (order.getSymbol().equals(currentSymbol)) {
                String timeLabel = timeLabels[random.nextInt(timeLabels.length)];
                
                if ("BUY".equals(order.getSide())) {
                    XYChart.Data<String, Number> buyData = new XYChart.Data<>(timeLabel, order.getPrice());
                    buyMarkers.getData().add(buyData);
                    buyData.nodeProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal != null) {
                            newVal.setStyle("-fx-background-color: #56d364; -fx-padding: 6px; " +
                                "-fx-border-width: 2; -fx-border-color: #2d9c4e; " +
                                "-fx-border-radius: 6; -fx-background-radius: 6;");
                        }
                    });
                } else if ("SELL".equals(order.getSide())) {
                    XYChart.Data<String, Number> sellData = new XYChart.Data<>(timeLabel, order.getPrice());
                    sellMarkers.getData().add(sellData);
                    sellData.nodeProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal != null) {
                            newVal.setStyle("-fx-background-color: #f85149; -fx-padding: 6px; " +
                                "-fx-border-width: 2; -fx-border-color: #da3633; " +
                                "-fx-border-radius: 6; -fx-background-radius: 6;");
                        }
                    });
                }
            }
        }
        
        // Add markers to chart
        if (!buyMarkers.getData().isEmpty()) {
            priceChart.getData().add(buyMarkers);
        }
        if (!sellMarkers.getData().isEmpty()) {
            priceChart.getData().add(sellMarkers);
        }
    }
    
    private void updatePriceLabels() {
        currentPriceLabel.setText(String.format("$%,.2f", currentPrice));
        priceField.setText(String.format("%.2f", currentPrice));
        
        // Simulate price change percentage
        double changePercent = (random.nextDouble() - 0.5) * 10; // +/- 5%
        boolean isPositive = changePercent > 0;
        priceChangeLabel.setText(String.format("%s%.2f%%", isPositive ? "+" : "", changePercent));
        priceChangeLabel.setStyle(String.format("-fx-text-fill: %s; -fx-font-size: 12;", 
            isPositive ? "#56d364" : "#f85149"));
    }
    
    private void updateMarketStats(String symbol, int index) {
        // Simulate market statistics
        double high = currentPrice * 1.05;
        double low = currentPrice * 0.95;
        double volume = (1 + random.nextDouble() * 5) * 1000000000; // 1-6 billion
        
        highLabel.setText(String.format("$%,.2f", high));
        lowLabel.setText(String.format("$%,.2f", low));
        volumeLabel.setText(formatVolume(volume));
        
        // Market cap and supply (simulated)
        double marketCap = currentPrice * (19000000 + random.nextInt(1000000));
        marketCapLabel.setText(formatVolume(marketCap));
        
        String[] supplies = {"19.5M BTC", "120M ETH", "150M BNB", "35B ADA", "450M SOL", "100B XRP"};
        supplyLabel.setText(supplies[index]);
        
        String[] aths = {"$69,000", "$4,878", "$686", "$3.10", "$260", "$3.84"};
        athLabel.setText(aths[index]);
    }
    
    private String formatVolume(double volume) {
        if (volume >= 1_000_000_000) {
            return String.format("$%.1fB", volume / 1_000_000_000);
        } else if (volume >= 1_000_000) {
            return String.format("$%.1fM", volume / 1_000_000);
        } else {
            return String.format("$%.0f", volume);
        }
    }
    
    private void updateUserBalance() {
        try {
            double balance = walletService.getUserBalance(authService.getCurrentUser().getId());
            balanceHeaderLabel.setText(String.format("Balance: $%,.2f", balance));
            availableBalanceLabel.setText(String.format("Available: $%,.2f", balance));
        } catch (Exception e) {
            balanceHeaderLabel.setText("Balance: $0.00");
            availableBalanceLabel.setText("Available: $0.00");
        }
    }
    
    private void startPriceUpdates() {
        // Update price every 3 seconds
        priceUpdateTimeline = new Timeline(new KeyFrame(Duration.seconds(3), event -> {
            // Simulate price movement
            currentPrice = currentPrice * (1 + (random.nextDouble() - 0.5) * 0.02); // +/- 1% change
            updatePriceLabels();
            
            // Check stop loss and take profit triggers for all open positions
            checkStopLossTakeProfitTriggers();
            
            // Update P/L display in order list
            refreshOrderList();
        }));
        priceUpdateTimeline.setCycleCount(Timeline.INDEFINITE);
        priceUpdateTimeline.play();
    }
    
    /**
     * Check all open positions for stop loss and take profit triggers
     * Automatically closes positions when SL or TP is hit
     */
    private void checkStopLossTakeProfitTriggers() {
        var orders = tradingService.getAllOrdersForUser(authService.getCurrentUser().getId());
        
        for (Order order : orders) {
            // Only check executed positions for the current symbol
            if (!"EXECUTED".equals(order.getStatus()) || !order.getSymbol().equals(currentSymbol)) {
                continue;
            }
            
            boolean triggered = false;
            String triggerType = "";
            double triggerPrice = 0;
            
            if ("BUY".equals(order.getSide())) {
                // LONG position
                // Stop Loss: price drops below SL level
                if (order.getStopLoss() > 0 && currentPrice <= order.getStopLoss()) {
                    triggered = true;
                    triggerType = "Stop Loss";
                    triggerPrice = order.getStopLoss();
                }
                // Take Profit: price rises above TP level
                else if (order.getTakeProfit() > 0 && currentPrice >= order.getTakeProfit()) {
                    triggered = true;
                    triggerType = "Take Profit";
                    triggerPrice = order.getTakeProfit();
                }
            } else {
                // SHORT position
                // Stop Loss: price rises above SL level
                if (order.getStopLoss() > 0 && currentPrice >= order.getStopLoss()) {
                    triggered = true;
                    triggerType = "Stop Loss";
                    triggerPrice = order.getStopLoss();
                }
                // Take Profit: price drops below TP level
                else if (order.getTakeProfit() > 0 && currentPrice <= order.getTakeProfit()) {
                    triggered = true;
                    triggerType = "Take Profit";
                    triggerPrice = order.getTakeProfit();
                }
            }
            
            if (triggered) {
                // Close the position at the trigger price
                Double profitLoss = tradingService.closePosition(order.getId(), triggerPrice);
                
                if (profitLoss != null) {
                    String positionType = "BUY".equals(order.getSide()) ? "LONG" : "SHORT";
                    String result = profitLoss >= 0 ? 
                        String.format("Profit: +$%.2f", profitLoss) : 
                        String.format("Loss: -$%.2f", Math.abs(profitLoss));
                    
                    System.out.println("DEBUG: " + triggerType + " triggered for order #" + order.getId() + 
                                      " at $" + triggerPrice + " - " + result);
                    
                    // Copy to final variables for lambda
                    final String finalTriggerType = triggerType;
                    final String finalPositionType = positionType;
                    final String finalSymbol = order.getSymbol();
                    final double finalTriggerPrice = triggerPrice;
                    final String finalResult = result;
                    
                    // Show notification to user
                    javafx.application.Platform.runLater(() -> {
                        AlertUtils.showInfo(finalTriggerType + " Triggered!", 
                            String.format("%s hit for %s position on %s\nClosed at: $%.2f\n%s", 
                                finalTriggerType, finalPositionType, finalSymbol, finalTriggerPrice, finalResult));
                        updateUserBalance();
                        updatePriceChart();
                    });
                }
            }
        }
    }

    private void setupPriceCalculation() {
        quantityField.textProperty().addListener((obs, oldVal, newVal) -> calculateTotalCost());
        priceField.textProperty().addListener((obs, oldVal, newVal) -> calculateTotalCost());
    }

    private void calculateTotalCost() {
        try {
            double quantity = Double.parseDouble(quantityField.getText());
            double price = Double.parseDouble(priceField.getText());
            double total = quantity * price;
            totalCostLabel.setText(String.format("Total: $%.2f", total));
        } catch (NumberFormatException e) {
            totalCostLabel.setText("Total: $0.00");
        }
    }

    @FXML
    private void handlePlaceOrder() {
        try {
            // Validate inputs
            String symbol = symbolCombo.getValue();
            double quantity = Double.parseDouble(quantityField.getText());
            double price = Double.parseDouble(priceField.getText());
            String orderType = orderTypeCombo.getValue();
            String side = sideCombo.getValue();

            if (symbol == null || quantity <= 0 || price <= 0) {
                AlertUtils.showWarning("Input Error", "Please enter valid values");
                return;
            }

            double stopLoss = parseDouble(stopLossField.getText().trim(), 0);
            double takeProfit = parseDouble(takeProfitField.getText().trim(), 0);
            
            // Debug: log parsed values
            System.out.println("DEBUG: stopLossField raw text: '" + stopLossField.getText() + "'");
            System.out.println("DEBUG: stopLossField trimmed: '" + stopLossField.getText().trim() + "'");
            System.out.println("DEBUG: Parsed stopLoss: " + stopLoss);
            System.out.println("DEBUG: takeProfitField raw text: '" + takeProfitField.getText() + "'");
            System.out.println("DEBUG: takeProfitField trimmed: '" + takeProfitField.getText().trim() + "'");
            System.out.println("DEBUG: Parsed takeProfit: " + takeProfit);

            // Check balance for BOTH BUY (long) and SELL (short) orders
            // Both require margin (money to open the position)
            double totalCost = quantity * price;
            double balance = walletService.getUserBalance(authService.getCurrentUser().getId());
            if (balance < totalCost) {
                AlertUtils.showWarning("Insufficient Balance", 
                    String.format("You need $%.2f margin but only have $%.2f", totalCost, balance));
                return;
            }

            // Place order
            Order order = tradingService.placeOrder(
                authService.getCurrentUser().getId(),
                symbol, orderType, side, price, quantity, stopLoss, takeProfit
            );

            if (order != null) {
                System.out.println("DEBUG: Order placed successfully: " + side + " " + quantity + " " + symbol);
                AlertUtils.showInfo("Success", String.format("Order placed: %s %s %s %s @ $%.2f", 
                    side, quantity, symbol, orderType, price));
                clearFields();
                System.out.println("DEBUG: Calling refreshOrderList()");
                refreshOrderList();
                System.out.println("DEBUG: Refreshing chart to show TP/SL lines and markers");
                updatePriceChart();
                updateUserBalance();
                System.out.println("DEBUG: Chart and balance updated");
            } else {
                System.out.println("DEBUG: Order placement failed - returned null");
                AlertUtils.showError("Error", "Failed to place order");
            }
        } catch (NumberFormatException e) {
            AlertUtils.showWarning("Input Error", "Please enter valid numbers");
        }
    }

    private double parseDouble(String text, double defaultValue) {
        try {
            return text.isEmpty() ? defaultValue : Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private void clearFields() {
        quantityField.clear();
        priceField.clear();
        stopLossField.clear();
        takeProfitField.clear();
        totalCostLabel.setText("Total: $0.00");
    }

    private void refreshOrderList() {
        orderListView.getItems().clear();
        displayedOrders.clear();
        
        // Get all orders for the user
        var allOrders = tradingService.getAllOrdersForUser(authService.getCurrentUser().getId());
        System.out.println("DEBUG: Refreshing order list - found " + allOrders.size() + " total orders");
        
        for (Order order : allOrders) {
            // Only show PENDING and EXECUTED (open positions)
            if (!"PENDING".equals(order.getStatus()) && !"EXECUTED".equals(order.getStatus())) {
                continue;
            }
            
            displayedOrders.add(order);
            
            String positionType = "BUY".equals(order.getSide()) ? "LONG" : "SHORT";
            String status = order.getStatus();
            double margin = order.getPrice() * order.getQuantity();
            
            String orderText;
            if ("EXECUTED".equals(status)) {
                // Calculate P/L for executed positions
                double pnl = calculatePnL(order);
                String pnlStr = pnl >= 0 ? String.format("+$%.2f", pnl) : String.format("-$%.2f", Math.abs(pnl));
                String pnlColor = pnl >= 0 ? "✓" : "✗";
                orderText = String.format("#%d %s %s %.4f @ $%.2f | Margin: $%.2f | P/L: %s %s [OPEN]", 
                    order.getId(), positionType, order.getSymbol(), order.getQuantity(), 
                    order.getPrice(), margin, pnlStr, pnlColor);
            } else {
                orderText = String.format("#%d %s %s %.4f @ $%.2f | Margin: $%.2f [%s]", 
                    order.getId(), positionType, order.getSymbol(), order.getQuantity(), 
                    order.getPrice(), margin, status);
            }
            
            System.out.println("DEBUG: Adding order to list: " + orderText);
            orderListView.getItems().add(orderText);
        }
        
        if (displayedOrders.isEmpty()) {
            orderListView.getItems().add("No open positions");
            System.out.println("DEBUG: No orders found for user");
        }
    }
    
    private double calculatePnL(Order order) {
        // Calculate unrealized P/L based on current price
        double entryPrice = order.getPrice();
        double quantity = order.getQuantity();
        
        if ("BUY".equals(order.getSide())) {
            // LONG: profit when price goes UP
            return (currentPrice - entryPrice) * quantity;
        } else {
            // SHORT: profit when price goes DOWN
            return (entryPrice - currentPrice) * quantity;
        }
    }
    
    private void handleClosePosition() {
        int selectedIndex = orderListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= displayedOrders.size()) {
            AlertUtils.showWarning("No Selection", "Please select an open position to close");
            return;
        }
        
        Order selectedOrder = displayedOrders.get(selectedIndex);
        
        if (!"EXECUTED".equals(selectedOrder.getStatus())) {
            AlertUtils.showWarning("Invalid Order", "You can only close EXECUTED positions. Use Cancel for PENDING orders.");
            return;
        }
        
        Double profitLoss = tradingService.closePosition(selectedOrder.getId(), currentPrice);
        
        if (profitLoss != null) {
            String result = profitLoss >= 0 ? 
                String.format("Profit: +$%.2f", profitLoss) : 
                String.format("Loss: -$%.2f", Math.abs(profitLoss));
            
            AlertUtils.showInfo("Position Closed", 
                String.format("Closed %s position on %s\n%s", 
                    "BUY".equals(selectedOrder.getSide()) ? "LONG" : "SHORT",
                    selectedOrder.getSymbol(), result));
            
            refreshOrderList();
            updateUserBalance();
            updatePriceChart();
        } else {
            AlertUtils.showError("Error", "Failed to close position");
        }
    }
    
    private void handleCancelOrder() {
        int selectedIndex = orderListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= displayedOrders.size()) {
            AlertUtils.showWarning("No Selection", "Please select an order to cancel");
            return;
        }
        
        Order selectedOrder = displayedOrders.get(selectedIndex);
        
        if (!"PENDING".equals(selectedOrder.getStatus())) {
            AlertUtils.showWarning("Invalid Order", "You can only cancel PENDING orders. Use Close for open positions.");
            return;
        }
        
        if (tradingService.cancelOrder(selectedOrder.getId())) {
            AlertUtils.showInfo("Order Cancelled", 
                String.format("Cancelled %s order for %s %s", 
                    "BUY".equals(selectedOrder.getSide()) ? "LONG" : "SHORT",
                    selectedOrder.getQuantity(), selectedOrder.getSymbol()));
            
            refreshOrderList();
            updateUserBalance();
        } else {
            AlertUtils.showError("Error", "Failed to cancel order");
        }
    }
    
    private double getAverageBuyPrice(String symbol, int userId) {
        var orders = tradingService.getPendingOrders(userId);
        double totalCost = 0;
        double totalQuantity = 0;
        
        for (Order order : orders) {
            if ("BUY".equals(order.getSide()) && symbol.equals(order.getSymbol())) {
                totalCost += order.getPrice() * order.getQuantity();
                totalQuantity += order.getQuantity();
            }
        }
        
        return totalQuantity > 0 ? totalCost / totalQuantity : 0;
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
            System.err.println("ERROR loading view: " + e.getMessage());
            e.printStackTrace();
            AlertUtils.showError("Navigation Error", "Could not load view: " + e.getMessage());
        }
    }
}
