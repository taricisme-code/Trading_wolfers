package com.tradingdemo.controller;

import java.io.IOException;
import java.util.Random;

import com.tradingdemo.model.Order;
import com.tradingdemo.service.AuthService;
import com.tradingdemo.service.BinanceApiService;
import com.tradingdemo.service.SentimentAnalysisService;
import com.tradingdemo.service.TradingService;
import com.tradingdemo.service.WalletService;
import com.tradingdemo.util.AlertUtils;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
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
    @FXML private Label balanceHeaderLabel;
    @FXML private Label quantityUnitLabel;
    @FXML private Label availableBalanceLabel;
    @FXML private Label marketCapChangeLabel;
    @FXML private Label volumeChangeLabel;
    
    // Crypto Tab Buttons
    @FXML private Button btcButton;
    @FXML private Button ethButton;
    @FXML private Button bnbButton;
    @FXML private Button adaButton;
    @FXML private Button solButton;
    @FXML private Button xrpButton;
    
    // Tab Buttons
    @FXML private Button chartTab;
    @FXML private Button marketsTab;
    @FXML private Button newsTab;
    @FXML private Button aboutTab;
    
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
    @FXML private Button decreaseBtn;
    @FXML private Button increaseBtn;
    @FXML private Button viewOrdersButton;
    @FXML private ListView<String> orderListView;
    @FXML private ToggleButton buyToggle;
    @FXML private ToggleButton sellToggle;
    @FXML private Button closePositionButton;
    @FXML private Button cancelOrderButton;
    @FXML private Button refreshOrdersButton;
    
    // Sentiment Analysis Components
    @FXML private Label sentimentIndexLabel;
    @FXML private Label sentimentSignalLabel;
    @FXML private Label sentimentReasoningLabel;
    @FXML private Button viewDetailedAnalysisButton;
    
    // Open Positions Labels
    @FXML private Label positionCountLabel;
    @FXML private Label totalPnLLabel;
    
    // Chart Control Buttons
    @FXML private Button priceChartBtn;
    @FXML private Button volumeChartBtn;
    @FXML private Button marketCapChartBtn;
    @FXML private Button time24hBtn;
    @FXML private Button time7dBtn;
    @FXML private Button time1mBtn;
    @FXML private Button time1yBtn;
    @FXML private Button timeAllBtn;
    
    // Chatbot Button
    @FXML private Button chatbotButton;
    
    // Chart state
    private String currentChartType = "PRICE"; // PRICE, VOLUME, MARKET_CAP
    private String currentTimeRange = "24H"; // 24H, 7D, 1M, 1Y, ALL
    
    // Store order IDs for the list
    private java.util.List<Order> displayedOrders = new java.util.ArrayList<>();

    private final TradingService tradingService = new TradingService();
    private final WalletService walletService = new WalletService();
    private final AuthService authService = new AuthService();
    private final BinanceApiService binanceApiService = new BinanceApiService();
    private final SentimentAnalysisService sentimentService = new SentimentAnalysisService();

    private static final String[] CRYPTOCURRENCIES = {"BTC", "ETH", "BNB", "ADA", "SOL", "XRP", "DOGE", "USDC"};
    private static final String[] ORDER_TYPES = {"MARKET", "LIMIT"};
    private static final String[] SIDES = {"BUY", "SELL"};
    private static final double[] BASE_PRICES = {45000, 2500, 350, 0.50, 140, 2.50, 0.08, 1.0};
    private static final String[] CRYPTO_NAMES = {"Bitcoin", "Ethereum", "Binance Coin", "Cardano", "Solana", "Ripple", "Dogecoin", "USD Coin"};
    
    private String currentSymbol = "BTC";
    private double currentPrice = BASE_PRICES[0];
    private Timeline priceUpdateTimeline;
    private Random random = new Random();
    private boolean useRealApi = true;
    private BinanceApiService.TickerData currentTickerData = null;

    @FXML
    public void initialize() {
        // Set custom cell factory for black text in order list
        if (orderListView != null) {
            orderListView.setCellFactory(lv -> {
                ListCell<String> cell = new ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setStyle(null);
                        } else {
                            setText(item);
                            setStyle("-fx-text-fill: #111827; -fx-font-size: 12px;");
                        }
                    }
                };
                return cell;
            });
        }
        
        setupComboBoxes();
        setupBuySellToggle();
        setupCryptoTabs();
        setupChartControls();
        setupPriceAdjustButtons();
        setupNavigationTabs();
        setupPriceCalculation();
        placeOrderButton.setOnAction(e -> handlePlaceOrder());
        backButton.setOnAction(e -> goBack());
        
        // Position management buttons
        closePositionButton.setOnAction(e -> handleClosePosition());
        if (cancelOrderButton != null) {
            cancelOrderButton.setOnAction(e -> handleCancelOrder());
        }
        refreshOrdersButton.setOnAction(e -> refreshOrderList());
        
        if (viewOrdersButton != null) {
            viewOrdersButton.setOnAction(e -> refreshOrderList());
        }
        
        // Sentiment analysis button
        if (viewDetailedAnalysisButton != null) {
            viewDetailedAnalysisButton.setOnAction(e -> showDetailedSentimentAnalysis());
        }
        
        // Chatbot button
        if (chatbotButton != null) {
            chatbotButton.setOnAction(e -> showChatbot());
        }
        
        // Initialize chart and price data
        updateUserBalance();
        switchToCrypto("BTC", 0);
        refreshOrderList();
        
        // Start live price updates
        startPriceUpdates();
        
        // Load initial sentiment data
        updateSentimentDisplay();
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
                placeOrderButton.setText("Place Trade");
                placeOrderButton.setStyle("-fx-padding: 12; -fx-font-size: 14; -fx-font-weight: 700; -fx-background-color: #3861fb; -fx-text-fill: white; -fx-cursor: hand; -fx-border-radius: 8; -fx-background-radius: 8; -fx-pref-width: 160;");
            }
        });
        
        sellToggle.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                sideCombo.setValue("SELL");
                placeOrderButton.setText("Place Trade");
                placeOrderButton.setStyle("-fx-padding: 12; -fx-font-size: 14; -fx-font-weight: 700; -fx-background-color: #ea3943; -fx-text-fill: white; -fx-cursor: hand; -fx-border-radius: 8; -fx-background-radius: 8; -fx-pref-width: 160;");
            }
        });
    }
    
    /**
     * Setup price adjustment +/- buttons
     */
    private void setupPriceAdjustButtons() {
        if (decreaseBtn != null) {
            decreaseBtn.setOnAction(e -> adjustPrice(-100));
        }
        if (increaseBtn != null) {
            increaseBtn.setOnAction(e -> adjustPrice(100));
        }
    }
    
    /**
     * Adjust price by specified amount
     */
    private void adjustPrice(double amount) {
        try {
            double currentVal = Double.parseDouble(priceField.getText().replace(",", "").replace("$", ""));
            double newVal = Math.max(0, currentVal + amount);
            priceField.setText(String.format("%.2f", newVal));
            calculateTotalCost();
        } catch (Exception e) {
            System.err.println("Error adjusting price: " + e.getMessage());
        }
    }
    
    /**
     * Setup navigation tabs (Chart, Markets, News, About)
     */
    private void setupNavigationTabs() {
        if (chartTab != null) {
            chartTab.setOnAction(e -> {
                // Chart tab is default, already showing
                updateTabStyles("chart");
            });
        }
        if (marketsTab != null) {
            marketsTab.setOnAction(e -> {
                updateTabStyles("markets");
                // Could navigate to markets view
            });
        }
        if (newsTab != null) {
            newsTab.setOnAction(e -> {
                updateTabStyles("news");
                loadView("/com/tradingdemo/view/news.fxml");
            });
        }
        if (aboutTab != null) {
            aboutTab.setOnAction(e -> {
                updateTabStyles("about");
                // Could show about info
            });
        }
    }
    
    /**
     * Update tab button styles
     */
    private void updateTabStyles(String activeTab) {
        String activeStyle = "-fx-background-color: transparent; -fx-text-fill: #3861fb; -fx-border-color: transparent transparent #3861fb transparent; -fx-border-width: 0 0 3 0; -fx-padding: 0 0 12 0; -fx-font-weight: 600; -fx-cursor: hand; -fx-font-size: 14;";
        String inactiveStyle = "-fx-background-color: transparent; -fx-text-fill: #6b7280; -fx-border-width: 0; -fx-padding: 0 0 12 0; -fx-font-weight: 500; -fx-cursor: hand; -fx-font-size: 14;";
        
        if (chartTab != null) {
            chartTab.setStyle("chart".equals(activeTab) ? activeStyle : inactiveStyle);
        }
        if (marketsTab != null) {
            marketsTab.setStyle("markets".equals(activeTab) ? activeStyle : inactiveStyle);
        }
        if (newsTab != null) {
            newsTab.setStyle("news".equals(activeTab) ? activeStyle : inactiveStyle);
        }
        if (aboutTab != null) {
            aboutTab.setStyle("about".equals(activeTab) ? activeStyle : inactiveStyle);
        }
    }
    
    private void setupCryptoTabs() {
        btcButton.setOnAction(e -> switchToCrypto("BTC", 0));
        ethButton.setOnAction(e -> switchToCrypto("ETH", 1));
        bnbButton.setOnAction(e -> switchToCrypto("BNB", 2));
        adaButton.setOnAction(e -> switchToCrypto("ADA", 3));
        solButton.setOnAction(e -> switchToCrypto("SOL", 4));
        xrpButton.setOnAction(e -> switchToCrypto("XRP", 5));
    }
    
    /**
     * Setup chart type and time range control buttons
     */
    private void setupChartControls() {
        // Chart type buttons
        if (priceChartBtn != null) {
            priceChartBtn.setOnAction(e -> switchChartType("PRICE"));
        }
        if (volumeChartBtn != null) {
            volumeChartBtn.setOnAction(e -> switchChartType("VOLUME"));
        }
        if (marketCapChartBtn != null) {
            marketCapChartBtn.setOnAction(e -> switchChartType("MARKET_CAP"));
        }
        
        // Time range buttons
        if (time24hBtn != null) {
            time24hBtn.setOnAction(e -> switchTimeRange("24H"));
        }
        if (time7dBtn != null) {
            time7dBtn.setOnAction(e -> switchTimeRange("7D"));
        }
        if (time1mBtn != null) {
            time1mBtn.setOnAction(e -> switchTimeRange("1M"));
        }
        if (time1yBtn != null) {
            time1yBtn.setOnAction(e -> switchTimeRange("1Y"));
        }
        if (timeAllBtn != null) {
            timeAllBtn.setOnAction(e -> switchTimeRange("ALL"));
        }
    }
    
    /**
     * Switch chart type (Price, Volume, Market Cap)
     */
    private void switchChartType(String chartType) {
        currentChartType = chartType;
        
        // Update button styles - light background style
        String activeStyle = "-fx-padding: 6 12; -fx-background-color: #e0e7ff; -fx-text-fill: #3861fb; -fx-cursor: hand; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 13; -fx-border-width: 0; -fx-font-weight: 600;";
        String inactiveStyle = "-fx-padding: 6 12; -fx-background-color: #f3f4f6; -fx-text-fill: #111827; -fx-cursor: hand; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 13; -fx-border-width: 0;";
        
        if (priceChartBtn != null) {
            priceChartBtn.setStyle("PRICE".equals(chartType) ? activeStyle : inactiveStyle);
        }
        if (volumeChartBtn != null) {
            volumeChartBtn.setStyle("VOLUME".equals(chartType) ? activeStyle : inactiveStyle);
        }
        if (marketCapChartBtn != null) {
            marketCapChartBtn.setStyle("MARKET_CAP".equals(chartType) ? activeStyle : inactiveStyle);
        }
        
        // Refresh chart with new type
        updatePriceChart();
    }
    
    /**
     * Switch time range (24H, 7D, 1M, 1Y, ALL)
     */
    private void switchTimeRange(String timeRange) {
        currentTimeRange = timeRange;
        
        // Update button styles - blue for active
        String activeStyle = "-fx-padding: 6 12; -fx-background-color: #3861fb; -fx-text-fill: white; -fx-cursor: hand; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 13; -fx-font-weight: 600;";
        String inactiveStyle = "-fx-padding: 6 12; -fx-background-color: #f3f4f6; -fx-text-fill: #111827; -fx-cursor: hand; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 13;";
        
        if (time24hBtn != null) {
            time24hBtn.setStyle("24H".equals(timeRange) ? activeStyle : inactiveStyle);
        }
        if (time7dBtn != null) {
            time7dBtn.setStyle("7D".equals(timeRange) ? activeStyle : inactiveStyle);
        }
        if (time1mBtn != null) {
            time1mBtn.setStyle("1M".equals(timeRange) ? activeStyle : inactiveStyle);
        }
        if (time1yBtn != null) {
            time1yBtn.setStyle("1Y".equals(timeRange) ? activeStyle : inactiveStyle);
        }
        if (timeAllBtn != null) {
            timeAllBtn.setStyle("ALL".equals(timeRange) ? activeStyle : inactiveStyle);
        }
        
        // Refresh chart with new time range
        updatePriceChart();
    }
    
    private void switchToCrypto(String symbol, int index) {
        currentSymbol = symbol;
        symbolCombo.setValue(symbol);
        
        // Update visual active state - pill style
        Button[] buttons = {btcButton, ethButton, bnbButton, adaButton, solButton, xrpButton};
        String activeStyle = "-fx-padding: 8 16; -fx-background-color: #3861fb; -fx-text-fill: white; -fx-cursor: hand; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 13;";
        String inactiveStyle = "-fx-padding: 8 16; -fx-background-color: white; -fx-text-fill: #6b7280; -fx-cursor: hand; -fx-border-radius: 8; -fx-background-radius: 8; -fx-border-color: #e5e7eb; -fx-border-width: 1; -fx-font-size: 13;";
        
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i] != null) {
                buttons[i].setStyle(i == index ? activeStyle : inactiveStyle);
            }
        }
        
        // Update labels
        selectedSymbolLabel.setText(CRYPTO_NAMES[index] + " " + symbol);
        quantityUnitLabel.setText("(" + symbol + ")");
        
        // Fetch real-time price for new symbol
        if (useRealApi) {
            new Thread(() -> {
                BinanceApiService.TickerData tickerData = binanceApiService.get24HourTicker(symbol);
                
                if (tickerData != null) {
                    currentTickerData = tickerData;
                    currentPrice = tickerData.currentPrice;
                    
                    Platform.runLater(() -> {
                        updatePriceLabelsWithRealData();
                        updateMarketStatsWithRealData();
                        updatePriceChart();
                        // Update sentiment for the new crypto
                        updateSentimentDisplay();
                    });
                } else {
                    currentPrice = BASE_PRICES[index];
                    Platform.runLater(() -> {
                        updatePriceChart();
                        updateMarketStats(symbol, index);
                        // Update sentiment even with fallback data
                        updateSentimentDisplay();
                    });
                }
            }).start();
        } else {
            currentPrice = BASE_PRICES[index];
            updatePriceChart();
            updateMarketStats(symbol, index);
            // Update sentiment with simulated data
            updateSentimentDisplay();
        }
    }
    
    private void updatePriceChart() {
        priceChart.getData().clear();
        
        // Get time labels based on selected range
        String[] timeLabels = getTimeLabelsForRange(currentTimeRange);
        int dataPoints = timeLabels.length;
        
        // Generate data based on chart type
        double[] dataValues = new double[dataPoints];
        double baseValue = getCurrentBaseValue();
        
        // Generate data with appropriate volatility
        double volatility = getVolatilityForRange(currentTimeRange);
        dataValues[0] = baseValue;
        
        for (int i = 1; i < dataPoints; i++) {
            double change = (random.nextDouble() - 0.5) * volatility;
            dataValues[i] = dataValues[i-1] * (1 + change);
        }
        
        // Create main series
        XYChart.Series<String, Number> mainSeries = new XYChart.Series<>();
        mainSeries.setName(getChartLabel());
        
        for (int i = 0; i < dataPoints; i++) {
            mainSeries.getData().add(new XYChart.Data<>(timeLabels[i], dataValues[i]));
        }
        
        priceChart.getData().add(mainSeries);
        
        // Style the chart line with gradient effect (matching HTML example)
        Platform.runLater(() -> {
            var seriesLine = priceChart.lookup(".chart-series-line");
            if (seriesLine != null) {
                // Green color like the HTML example
                seriesLine.setStyle("-fx-stroke: #16c784; -fx-stroke-width: 3px;");
            }
            
            // Add fill under the line
            var seriesArea = priceChart.lookup(".chart-series-area-fill");
            if (seriesArea != null) {
                // Gradient fill from green to transparent
                seriesArea.setStyle("-fx-fill: linear-gradient(to bottom, rgba(22,199,132,0.35), rgba(22,199,132,0.02));");
            }
        });
        
        // Add TP/SL lines if in price mode
        if ("PRICE".equals(currentChartType)) {
            addTPSLLines(timeLabels, dataValues);
        }
        
        // Update current price from last data point
        if ("PRICE".equals(currentChartType)) {
            currentPrice = dataValues[dataValues.length - 1];
            updatePriceLabels();
        }
    }
    
    /**
     * Get time labels based on selected time range
     */
    private String[] getTimeLabelsForRange(String range) {
        switch (range) {
            case "24H":
                return new String[]{"00:00", "02:00", "04:00", "06:00", "08:00", "10:00", "12:00", 
                                   "14:00", "16:00", "18:00", "20:00", "22:00", "24:00"};
            case "7D":
                return new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
            case "1M":
                return new String[]{"Week 1", "Week 2", "Week 3", "Week 4"};
            case "1Y":
                return new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", 
                                   "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            case "ALL":
                return new String[]{"2020", "2021", "2022", "2023", "2024", "2025", "2026"};
            default:
                return new String[]{"00:00", "04:00", "08:00", "12:00", "16:00", "20:00", "24:00"};
        }
    }
    
    /**
     * Get base value based on chart type
     */
    private double getCurrentBaseValue() {
        switch (currentChartType) {
            case "PRICE":
                return currentPrice;
            case "VOLUME":
                // Simulated 24h volume
                return currentTickerData != null ? currentTickerData.volume : 1000000000;
            case "MARKET_CAP":
                // Simulated market cap
                return currentPrice * 19000000; // Approximate circulating supply
            default:
                return currentPrice;
        }
    }
    
    /**
     * Get volatility factor based on time range
     */
    private double getVolatilityForRange(String range) {
        switch (range) {
            case "24H":
                return 0.02; // 2% per interval
            case "7D":
                return 0.05; // 5% per day
            case "1M":
                return 0.08; // 8% per week
            case "1Y":
                return 0.12; // 12% per month
            case "ALL":
                return 0.20; // 20% per year
            default:
                return 0.03;
        }
    }
    
    /**
     * Get chart label based on type
     */
    private String getChartLabel() {
        switch (currentChartType) {
            case "PRICE":
                return currentSymbol + " Price";
            case "VOLUME":
                return "24h Volume";
            case "MARKET_CAP":
                return "Market Capitalization";
            default:
                return "Price";
        }
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
    
    /**
     * Updates price labels with real-time Binance data
     */
    private void updatePriceLabelsWithRealData() {
        if (currentTickerData == null) {
            updatePriceLabels();
            return;
        }
        
        currentPriceLabel.setText(String.format("$%,.2f", currentTickerData.currentPrice));
        priceField.setText(String.format("%.2f", currentTickerData.currentPrice));
        
        // Real price change percentage from API
        double changePercent = currentTickerData.priceChangePercent;
        boolean isPositive = changePercent > 0;
        priceChangeLabel.setText(String.format("%s%.2f%%", isPositive ? "+" : "", changePercent));
        priceChangeLabel.setStyle(String.format("-fx-text-fill: %s; -fx-font-size: 12;", 
            isPositive ? "#56d364" : "#f85149"));
    }
    
    /**
     * Updates price labels with simulated data (fallback)
     */
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
    
    /**
     * Updates market stats with real-time Binance data
     */
    private void updateMarketStatsWithRealData() {
        if (currentTickerData == null) {
            return;
        }
        
        // Real 24h high/low from Binance
        highLabel.setText(String.format("$%,.2f", currentTickerData.highPrice));
        lowLabel.setText(String.format("$%,.2f", currentTickerData.lowPrice));
        
        // Real 24h volume (quote volume is in USD)
        volumeLabel.setText(formatVolume(currentTickerData.quoteVolume));
        
        // Estimate market cap (supply data would need additional API)
        String[] supplies = {"19.5M", "120M", "150M", "35B", "450M", "100B", "141B", "24B"};
        double[] supplyValues = {19500000, 120000000, 150000000, 35000000000.0, 450000000, 100000000000.0, 141000000000.0, 24000000000.0};
        
        int symbolIndex = java.util.Arrays.asList(CRYPTOCURRENCIES).indexOf(currentSymbol);
        if (symbolIndex >= 0 && symbolIndex < supplyValues.length) {
            double marketCap = currentPrice * supplyValues[symbolIndex];
            marketCapLabel.setText(formatVolume(marketCap));
            supplyLabel.setText(supplies[symbolIndex] + " " + currentSymbol);
        }
    }
    
    /**
     * Updates market stats with simulated data (fallback)
     */
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
        // Test API connection first
        new Thread(() -> {
            boolean apiAvailable = binanceApiService.testConnection();
            useRealApi = apiAvailable;
            
            Platform.runLater(() -> {
                if (useRealApi) {
                    System.out.println("✓ Connected to Binance API - using real-time data");
                    balanceHeaderLabel.setStyle("-fx-text-fill: #56d364; -fx-font-size: 14; -fx-font-weight: bold;");
                } else {
                    System.out.println("⚠ Binance API unavailable - using simulated data");
                    AlertUtils.showWarning("API Status", "Binance API is unavailable.\nUsing simulated prices as fallback.");
                }
            });
        }).start();
        
        // Update price every 5 seconds (real-time updates)
        priceUpdateTimeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> {
            if (useRealApi) {
                // Fetch real price from Binance API in background thread
                new Thread(() -> {
                    try {
                        BinanceApiService.TickerData tickerData = binanceApiService.get24HourTicker(currentSymbol);
                        
                        if (tickerData != null) {
                            currentTickerData = tickerData;
                            currentPrice = tickerData.currentPrice;
                            
                            Platform.runLater(() -> {
                                updatePriceLabelsWithRealData();
                                updateMarketStatsWithRealData();
                            });
                        } else {
                            // API call failed, switch to fallback
                            Platform.runLater(() -> {
                                if (useRealApi) {
                                    useRealApi = false;
                                    System.err.println("⚠ Binance API call failed - switching to simulated data");
                                    AlertUtils.showWarning("API Error", "Lost connection to Binance API.\nSwitching to simulated prices.");
                                }
                            });
                            simulatePriceMovement();
                        }
                    } catch (Exception e) {
                        System.err.println("Error fetching price: " + e.getMessage());
                        simulatePriceMovement();
                    }
                }).start();
            } else {
                // Use simulated price movement
                simulatePriceMovement();
            }
            
            // Check stop loss and take profit triggers for all open positions
            checkStopLossTakeProfitTriggers();
            
            // Update P/L display in order list
            refreshOrderList();
            
            // Update sentiment every 60 seconds (every 12th iteration at 5-second intervals)
            if (priceUpdateTimeline.getCurrentTime().toSeconds() % 60 < 5) {
                updateSentimentDisplay();
            }
        }));
        priceUpdateTimeline.setCycleCount(Timeline.INDEFINITE);
        priceUpdateTimeline.play();
    }
    
    /**
     * Simulates price movement when API is unavailable
     */
    private void simulatePriceMovement() {
        currentPrice = currentPrice * (1 + (random.nextDouble() - 0.5) * 0.02); // +/- 1% change
        Platform.runLater(this::updatePriceLabels);
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
        
        double totalPnL = 0.0;
        int positionCount = 0;
        
        for (Order order : allOrders) {
            // Only show PENDING and EXECUTED (open positions)
            if (!"PENDING".equals(order.getStatus()) && !"EXECUTED".equals(order.getStatus())) {
                continue;
            }
            
            displayedOrders.add(order);
            positionCount++;
            
            String positionType = "BUY".equals(order.getSide()) ? "LONG" : "SHORT";
            String status = order.getStatus();
            double margin = order.getPrice() * order.getQuantity();
            
            String orderText;
            if ("EXECUTED".equals(status)) {
                // Calculate P/L for executed positions
                double pnl = calculatePnL(order);
                totalPnL += pnl;
                String pnlStr = pnl >= 0 ? String.format("+%.2f", pnl) : String.format("%.2f", pnl);
                String arrow = pnl >= 0 ? "▲" : "▼";
                String pnlColor = pnl >= 0 ? "+" : "-";
                
                // Format: BTC/USD | LONG | 0.5000 | $69,034 | ▲ +234.50
                orderText = String.format("%s | %s | %.4f | $%,.2f | %s %s%s", 
                    order.getSymbol(), positionType, order.getQuantity(), 
                    order.getPrice(), arrow, pnlColor, pnlStr);
            } else {
                // Pending order format
                orderText = String.format("%s | %s | %.4f | $%,.2f | PENDING", 
                    order.getSymbol(), positionType, order.getQuantity(), order.getPrice());
            }
            
            System.out.println("DEBUG: Adding order to list: " + orderText);
            orderListView.getItems().add(orderText);
        }
        
        // Update position count label
        if (positionCountLabel != null) {
            if (positionCount == 0) {
                positionCountLabel.setText("No positions");
            } else if (positionCount == 1) {
                positionCountLabel.setText("1 position");
            } else {
                positionCountLabel.setText(positionCount + " positions");
            }
        }
        
        // Update total P/L label
        if (totalPnLLabel != null) {
            if (positionCount == 0) {
                totalPnLLabel.setText("Total P/L: --");
                totalPnLLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 13; -fx-font-weight: bold;");
            } else {
                String pnlText = totalPnL >= 0 ? 
                    String.format("Total P/L: +$%.2f", totalPnL) : 
                    String.format("Total P/L: -$%.2f", Math.abs(totalPnL));
                String color = totalPnL >= 0 ? "#16c784" : "#ea3943";
                totalPnLLabel.setText(pnlText);
                totalPnLLabel.setStyle(String.format("-fx-text-fill: %s; -fx-font-size: 13; -fx-font-weight: bold;", color));
            }
        }
        
        if (displayedOrders.isEmpty()) {
            orderListView.getItems().add("No open positions - Start trading to see your positions here");
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
    
    /**
     * Updates the sentiment display panel with crypto-specific sentiment
     */
    private void updateSentimentDisplay() {
        if (sentimentIndexLabel == null) return;
        
        new Thread(() -> {
            try {
                // Get sentiment for the currently selected cryptocurrency
                SentimentAnalysisService.SentimentData sentiment = sentimentService.getCryptoSentiment(currentSymbol);
                
                if (sentiment != null) {
                    Platform.runLater(() -> {
                        // Update index label with crypto symbol
                        String cryptoName = getCryptoFullName(currentSymbol);
                        sentimentIndexLabel.setText(sentiment.fearGreedIndex + "/100 - " + sentiment.classification);
                        
                        // Update recommendation label with color coding
                        sentimentSignalLabel.setText(sentiment.recommendation);
                        if ("BUY".equals(sentiment.signal) || "STRONG BUY".equals(sentiment.recommendation)) {
                            sentimentSignalLabel.setStyle("-fx-text-fill: #56d364; -fx-font-size: 11; -fx-font-weight: bold;");
                        } else if ("SELL".equals(sentiment.signal) || "STRONG SELL".equals(sentiment.recommendation)) {
                            sentimentSignalLabel.setStyle("-fx-text-fill: #f85149; -fx-font-size: 11; -fx-font-weight: bold;");
                        } else {
                            sentimentSignalLabel.setStyle("-fx-text-fill: #f0883e; -fx-font-size: 11; -fx-font-weight: bold;");
                        }
                        
                        // Update reasoning (shortened for display)
                        String shortReasoning = sentiment.reasoning.length() > 120 ? 
                            sentiment.reasoning.substring(0, 117) + "..." : sentiment.reasoning;
                        sentimentReasoningLabel.setText(shortReasoning);
                        
                        System.out.println("Updated sentiment for " + cryptoName + ": " + sentiment.recommendation);
                    });
                } else {
                    Platform.runLater(() -> {
                        sentimentIndexLabel.setText("N/A");
                        sentimentSignalLabel.setText("--");
                        sentimentReasoningLabel.setText("Unable to fetch sentiment data for " + currentSymbol);
                    });
                }
            } catch (Exception e) {
                System.err.println("Error updating sentiment: " + e.getMessage());
                Platform.runLater(() -> {
                    sentimentIndexLabel.setText("Error");
                    sentimentSignalLabel.setText("--");
                    sentimentReasoningLabel.setText("Sentiment service unavailable");
                });
            }
        }).start();
    }
    
    /**
     * Gets full cryptocurrency name from symbol
     */
    private String getCryptoFullName(String symbol) {
        switch (symbol.toUpperCase()) {
            case "BTC": return "Bitcoin";
            case "ETH": return "Ethereum";
            case "BNB": return "Binance Coin";
            case "ADA": return "Cardano";
            case "SOL": return "Solana";
            case "XRP": return "Ripple";
            case "DOGE": return "Dogecoin";
            case "USDC": return "USD Coin";
            default: return symbol;
        }
    }
    
    /**
     * Shows detailed sentiment analysis in a dialog
     */
    private void showDetailedSentimentAnalysis() {
        new Thread(() -> {
            // Get crypto-specific sentiment analysis
            SentimentAnalysisService.SentimentData sentiment = sentimentService.getCryptoSentiment(currentSymbol);
            
            if (sentiment != null) {
                String cryptoName = getCryptoFullName(currentSymbol);
                StringBuilder analysis = new StringBuilder();
                analysis.append("═══════════════════════════════════════════════\n");
                analysis.append("        ").append(cryptoName.toUpperCase()).append(" SENTIMENT ANALYSIS\n");
                analysis.append("═══════════════════════════════════════════════\n\n");
                
                if (sentiment.currentPrice > 0) {
                    analysis.append("Current Price: $").append(String.format("%,.2f", sentiment.currentPrice)).append("\n");
                    analysis.append("24h Change: ").append(String.format("%+.2f%%", sentiment.priceChange24h)).append("\n");
                    analysis.append("24h Volume: $").append(String.format("%,.0f", sentiment.volume24h)).append("\n\n");
                }
                
                analysis.append("Fear & Greed Index: ").append(sentiment.fearGreedIndex).append("/100\n");
                analysis.append("Classification: ").append(sentiment.classification.toUpperCase()).append("\n\n");
                
                analysis.append("───────────────────────────────────────────────\n");
                analysis.append("RECOMMENDATION: ").append(sentiment.recommendation).append("\n");
                analysis.append("Signal: ").append(sentiment.signal).append("\n");
                analysis.append("Confidence: ").append(sentiment.confidence).append("%\n");
                analysis.append("───────────────────────────────────────────────\n\n");
                
                analysis.append(cryptoName.toUpperCase()).append(" ANALYSIS:\n");
                analysis.append(sentiment.reasoning).append("\n\n");
                
                analysis.append("───────────────────────────────────────────────\n");
                analysis.append("💡 This analysis combines market-wide Fear & Greed\n");
                analysis.append("   Index with ").append(cryptoName).append("-specific price action and volume.\n");
                
                String detailedAnalysis = analysis.toString();
                Platform.runLater(() -> {
                    AlertUtils.showInfo(cryptoName + " Sentiment Analysis", detailedAnalysis);
                });
            } else {
                Platform.runLater(() -> {
                    AlertUtils.showError("Error", "Unable to fetch sentiment data for " + currentSymbol);
                });
            }
        }).start();
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
    
    private void showChatbot() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tradingdemo/view/chatbot.fxml"));
            Parent chatRoot = loader.load();
            ChatbotController chatController = loader.getController();
            
            // Create a popup stage
            Stage chatStage = new Stage();
            chatStage.setTitle("Trading Assistant");
            chatStage.initOwner(chatbotButton.getScene().getWindow());
            chatStage.setScene(new Scene(chatRoot));
            chatStage.setResizable(false);
            
            // Set close callback
            chatController.setOnCloseCallback(() -> chatStage.close());
            
            // Position on the right side
            Stage mainStage = (Stage) chatbotButton.getScene().getWindow();
            chatStage.setX(mainStage.getX() + mainStage.getWidth() - 450);
            chatStage.setY(mainStage.getY() + 100);
            
            chatStage.show();
        } catch (IOException e) {
            System.err.println("ERROR loading chatbot: " + e.getMessage());
            e.printStackTrace();
            AlertUtils.showError("Chatbot Error", "Could not load chatbot: " + e.getMessage());
        }
    }
}
