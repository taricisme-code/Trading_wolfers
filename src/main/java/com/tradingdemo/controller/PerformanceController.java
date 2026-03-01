package com.tradingdemo.controller;

import com.tradingdemo.model.PerformanceMetrics;
import com.tradingdemo.service.PerformanceService;
import com.tradingdemo.service.AuthService;
import com.tradingdemo.util.AlertUtils;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import java.io.IOException;

public class PerformanceController {

    @FXML private Label totalTradesLabel;
    @FXML private Label winRateLabel;
    @FXML private Label realizedPnlLabel;
    @FXML private Label avgProfitLabel;
    @FXML private LineChart<Number, Number> equityChart;
    @FXML private Button refreshButton;
    @FXML private Button backButton;

    private final PerformanceService perfService = new PerformanceService();

    @FXML
    public void initialize() {
        refreshButton.setOnAction(e -> loadMetrics());
        backButton.setOnAction(e -> goBack());
        loadMetrics();
    }

    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tradingdemo/view/dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root);
            // Apply stylesheet if available
            try {
                String css = getClass().getResource("/com/tradingdemo/view/styles.css").toExternalForm();
                scene.getStylesheets().add(css);
            } catch (Exception ex) {
                System.err.println("Warning: Could not load stylesheet: " + ex.getMessage());
            }
            stage.setScene(scene);
        } catch (IOException e) {
            System.err.println("ERROR navigating back to dashboard: " + e.getMessage());
            e.printStackTrace();
            AlertUtils.showError("Navigation Error", "Could not load dashboard: " + e.getMessage());
        }
    }

    private void loadMetrics() {
        var current = AuthService.getCurrentUser();
        if (current == null) return;

        PerformanceMetrics m = perfService.computeForUser(current.getId());

        totalTradesLabel.setText("Total Trades: " + m.totalTrades);
        winRateLabel.setText(String.format("Win Rate: %.1f%%", m.winRate * 100.0));
        realizedPnlLabel.setText(String.format("Realized P&L: $%.2f", m.realizedPnl));
        avgProfitLabel.setText(String.format("Avg P&L/Trade: $%.2f", m.avgProfitPerTrade));

        // Populate equity chart
        equityChart.getData().clear();
        XYChart.Series<Number, Number> s = new XYChart.Series<>();
        s.setName("Equity");
        for (int i = 0; i < m.equityValues.size(); i++) {
            s.getData().add(new XYChart.Data<>(i + 1, m.equityValues.get(i)));
        }
        equityChart.getData().add(s);
    }
}
