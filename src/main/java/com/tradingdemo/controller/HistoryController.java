package com.tradingdemo.controller;

import java.io.IOException;
import java.util.List;

import com.tradingdemo.model.Trade;
import com.tradingdemo.service.AuthService;
import com.tradingdemo.service.TradingService;
import com.tradingdemo.util.AlertUtils;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.stage.Stage;

/**
 * HistoryController - Displays user's trade history
 */
public class HistoryController {

    @FXML private ListView<String> historyListView;
    @FXML private Button refreshButton;
    @FXML private Button backButton;

    private final TradingService tradingService = new TradingService();
    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        // Set custom cell factory for black text
        historyListView.setCellFactory(lv -> {
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
        
        refreshButton.setOnAction(e -> refreshHistory());
        backButton.setOnAction(e -> goBack());
        refreshHistory();
    }

    private void refreshHistory() {
        historyListView.getItems().clear();
        int userId = authService.getCurrentUser().getId();
        List<Trade> trades = tradingService.getTradeHistory(userId);
        
        for (Trade trade : trades) {
            historyListView.getItems().add(String.format(
                "%s: %s %.8f %s @ $%.2f | Total: $%.2f | Time: %s",
                trade.getSide(), 
                trade.getId(),
                trade.getQuantity(),
                trade.getSymbol(),
                trade.getExecutedPrice(),
                trade.getQuantity() * trade.getExecutedPrice(),
                trade.getExecutedAt()
            ));
        }
        
        if (trades.isEmpty()) {
            historyListView.getItems().add("No trades yet");
        }
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
