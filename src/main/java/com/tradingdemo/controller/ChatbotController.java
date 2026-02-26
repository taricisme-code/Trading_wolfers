package com.tradingdemo.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.application.Platform;
import javafx.geometry.Insets;

import java.util.HashMap;
import java.util.Map;

public class ChatbotController {

    @FXML private VBox chatMessagesBox;
    @FXML private TextField chatInputField;
    @FXML private Button sendMessageButton;
    @FXML private Button closeChatButton;
    @FXML private Button quickAction1;
    @FXML private Button quickAction2;
    @FXML private Button quickAction3;
    @FXML private ScrollPane chatScrollPane;
    
    private Runnable onCloseCallback;
    
    private Map<String, String> responses = new HashMap<>();
    
    @FXML
    public void initialize() {
        setupResponses();
        setupEventHandlers();
        
        // Welcome message
        Platform.runLater(() -> {
            addBotMessage("Hello! 👋 I'm your Trading Assistant. How can I help you today?");
        });
    }
    
    private void setupResponses() {
        responses.put("how to trade", "To trade:\n1. Select a cryptocurrency (BTC, ETH, etc.)\n2. Choose BUY or SELL\n3. Enter the amount\n4. Click 'Place Trade'\n\nYou can also set order types like Limit or Stop-Loss for more control!");
        
        responses.put("view balance", "You can view your balance:\n• Top right of the trading page\n• Dashboard home page\n• Wallet section\n\nYour current balance updates automatically after each trade!");
        
        responses.put("market info", "Market information includes:\n• Current price and 24h change\n• Market cap and volume\n• 24h high/low prices\n• Total supply\n\nCheck the left sidebar on the trading page for real-time stats!");
        
        responses.put("wallet", "Your wallet shows:\n• Total balance\n• All cryptocurrency holdings\n• Current value of each asset\n\nAccess it from the Dashboard → Wallet button");
        
        responses.put("history", "Trade history shows:\n• All your past trades\n• Timestamps\n• Buy/sell prices\n• Profit/loss\n\nFind it on Dashboard → History");
        
        responses.put("help", "I can help you with:\n• How to place trades\n• View your balance\n• Understanding market data\n• Navigate the platform\n• Manage your wallet\n• View trade history\n\nJust ask me anything!");
        
        responses.put("news", "Stay updated with crypto news:\n• Visit Dashboard → News\n• Get latest articles from multiple sources\n• Market trends and analysis\n• Breaking crypto news");
        
        responses.put("profile", "Manage your profile:\n• Update personal info\n• Change password\n• View account details\n\nAccess via Dashboard → Profile");
        
        responses.put("order", "Order types available:\n• Market Order - Execute immediately at current price\n• Limit Order - Set your desired price\n• Stop-Loss - Automatic sell at specified price\n\nChoose from the 'Order Type' dropdown!");
        
        responses.put("position", "Open Positions:\n• View at bottom of trading page\n• Shows all active trades\n• Real-time P/L (Profit/Loss)\n• Click 'Close Position' to exit\n\nTotal P/L displayed on the right!");
    }
    
    private void setupEventHandlers() {
        sendMessageButton.setOnAction(e -> sendMessage());
        
        chatInputField.setOnAction(e -> sendMessage());
        
        quickAction1.setOnAction(e -> {
            chatInputField.setText("How to trade?");
            sendMessage();
        });
        
        quickAction2.setOnAction(e -> {
            chatInputField.setText("View balance");
            sendMessage();
        });
        
        quickAction3.setOnAction(e -> {
            chatInputField.setText("Market info");
            sendMessage();
        });
        
        closeChatButton.setOnAction(e -> {
            if (onCloseCallback != null) {
                onCloseCallback.run();
            }
        });
    }
    
    private void sendMessage() {
        String message = chatInputField.getText().trim();
        if (message.isEmpty()) {
            return;
        }
        
        // Add user message
        addUserMessage(message);
        chatInputField.clear();
        
        // Get bot response
        String response = getBotResponse(message);
        
        // Simulate typing delay
        Platform.runLater(() -> {
            try {
                Thread.sleep(500);
                addBotMessage(response);
            } catch (InterruptedException ex) {
                addBotMessage(response);
            }
        });
    }
    
    private String getBotResponse(String userMessage) {
        String lowerMessage = userMessage.toLowerCase();
        
        // Check for keywords in user message
        for (Map.Entry<String, String> entry : responses.entrySet()) {
            if (lowerMessage.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        // Default response
        return "I understand you're asking about: \"" + userMessage + "\"\n\n" +
               "I can help you with:\n" +
               "• Trading and orders\n" +
               "• Balance and wallet\n" +
               "• Market information\n" +
               "• Platform navigation\n\n" +
               "Try asking: 'How to trade?' or 'View balance'";
    }
    
    private void addUserMessage(String message) {
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(300);
        messageLabel.setStyle(
            "-fx-background-color: #3861fb; " +
            "-fx-text-fill: white; " +
            "-fx-padding: 10 15; " +
            "-fx-background-radius: 15 15 5 15; " +
            "-fx-font-size: 13;"
        );
        
        HBox messageBox = new HBox(messageLabel);
        messageBox.setAlignment(Pos.CENTER_RIGHT);
        messageBox.setPadding(new Insets(5, 0, 5, 50));
        
        chatMessagesBox.getChildren().add(messageBox);
        scrollToBottom();
    }
    
    private void addBotMessage(String message) {
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(300);
        messageLabel.setStyle(
            "-fx-background-color: white; " +
            "-fx-text-fill: #111827; " +
            "-fx-padding: 10 15; " +
            "-fx-background-radius: 15 15 15 5; " +
            "-fx-font-size: 13; " +
            "-fx-border-color: #e5e7eb; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 15 15 15 5;"
        );
        
        HBox messageBox = new HBox(messageLabel);
        messageBox.setAlignment(Pos.CENTER_LEFT);
        messageBox.setPadding(new Insets(5, 50, 5, 0));
        
        chatMessagesBox.getChildren().add(messageBox);
        scrollToBottom();
    }
    
    private void scrollToBottom() {
        Platform.runLater(() -> {
            chatScrollPane.setVvalue(1.0);
        });
    }
    
    public void setOnCloseCallback(Runnable callback) {
        this.onCloseCallback = callback;
    }
}
