package com.tradingdemo.controller;

import com.tradingdemo.dao.AlertDAO;
import com.tradingdemo.model.AlertRule;
import com.tradingdemo.service.AuthService;
import com.tradingdemo.util.AlertUtils;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class AlertsController {
    @FXML private TextField symbolField;
    @FXML private TextField priceField;
    @FXML private Button aboveBtn;
    @FXML private Button belowBtn;
    @FXML private ListView<AlertRule> alertsList;
    @FXML private Button enableBtn;
    @FXML private Button disableBtn;
    @FXML private Button deleteBtn;
    @FXML private Button sendTestBtn;
    @FXML private Button backBtn;

    private final AlertDAO alertDAO = new AlertDAO();

    public void initialize() {
        aboveBtn.setOnAction(e -> createAlert(true));
        belowBtn.setOnAction(e -> createAlert(false));
        enableBtn.setOnAction(e -> setSelectedEnabled(true));
        disableBtn.setOnAction(e -> setSelectedEnabled(false));
        if (sendTestBtn != null) sendTestBtn.setOnAction(e -> sendTestEmail());
        if (deleteBtn != null) deleteBtn.setOnAction(e -> deleteSelected());
        backBtn.setOnAction(e -> goBack());
        setupListCellFactory();
        refreshList();
    }

    private void deleteSelected() {
        AlertRule sel = alertsList.getSelectionModel().getSelectedItem();
        if (sel == null) {
            AlertUtils.showWarning("No Selection", "Please select an alert to delete.");
            return;
        }
        boolean confirmed = AlertUtils.showConfirmation("Confirm Delete", String.format("Delete alert for %s at %.4f?", sel.getSymbol(), sel.getTargetPrice()));
        if (!confirmed) return;
        try {
            if (alertDAO.deleteAlert(sel.getId())) {
                AlertUtils.showInfo("Deleted", "Alert deleted successfully.");
                refreshList();
            } else {
                AlertUtils.showError("Delete Failed", "Failed to delete alert.");
            }
        } catch (Exception e) {
            AlertUtils.showError("Error", "Error deleting alert: " + e.getMessage());
        }
    }

    private void setupListCellFactory() {
        alertsList.setCellFactory(lv -> {
            ListCell<AlertRule> cell = new ListCell<>() {
                @Override
                protected void updateItem(AlertRule item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle(null);
                        setContextMenu(null);
                    } else {
                        String cmp = item.isNotifyWhenAbove() ? ">=" : "<=";
                        String text = String.format("%s  |  target: %.4f %s  |  %s",
                                item.getSymbol(), item.getTargetPrice(), cmp, item.isEnabled() ? "ENABLED" : "DISABLED");
                        setText(text);
                        // color for enabled/disabled
                        if (item.isEnabled()) {
                            setStyle("-fx-text-fill: #16c784; -fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-text-fill: #6b7280;");
                        }
                        // context menu to toggle enabled state
                        MenuItem toggle = new MenuItem(item.isEnabled() ? "Disable" : "Enable");
                        toggle.setOnAction(ev -> {
                            item.setEnabled(!item.isEnabled());
                            if (alertDAO.updateAlert(item)) refreshList();
                        });
                        ContextMenu menu = new ContextMenu(toggle);
                        setContextMenu(menu);
                    }
                }
            };
            return cell;
        });
    }

    private void sendTestEmail() {
        var user = AuthService.getCurrentUser();
        if (user == null) { AlertUtils.showError("Error", "No user logged in."); return; }
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            AlertUtils.showWarning("No Email", "Your account has no email configured.");
            return;
        }

        try {
            var notifier = com.tradingdemo.notification.NotifierFactory.getNotifier();
            String subject = "Test Alert from Trading_wolfers";
            String body = "This is a test alert email sent to confirm email notifications are working.";
            notifier.sendEmail(user.getEmail(), subject, body);
            AlertUtils.showInfo("Test Sent", "Test email sent to " + user.getEmail());
        } catch (Exception ex) {
            System.err.println("Failed to send test email: " + ex.getMessage());
            AlertUtils.showError("Send Error", "Failed to send test email: " + ex.getMessage());
        }
    }

    private void refreshList() {
        var user = AuthService.getCurrentUser();
        if (user == null) return;
        alertsList.getItems().setAll(alertDAO.getAlertsByUser(user.getId()));
    }

    private void createAlert(boolean above) {
        var user = AuthService.getCurrentUser();
        if (user == null) { AlertUtils.showError("Error", "No user logged in."); return; }
        try {
            String sym = symbolField.getText().trim().toUpperCase();
            double p = Double.parseDouble(priceField.getText().trim());
            AlertRule a = new AlertRule(user.getId(), sym, p, above);
            if (alertDAO.createAlert(a)) {
                AlertUtils.showInfo("Alert Created", "Alert created for " + sym);
                refreshList();
            } else AlertUtils.showError("Error", "Failed to create alert");
        } catch (NumberFormatException nfe) {
            AlertUtils.showWarning("Input Error", "Invalid price");
        }
    }

    private void setSelectedEnabled(boolean enabled) {
        AlertRule sel = alertsList.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        sel.setEnabled(enabled);
        if (alertDAO.updateAlert(sel)) refreshList();
    }

    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tradingdemo/view/dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backBtn.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (IOException e) {
            AlertUtils.showError("Navigation Error", "Could not load dashboard: " + e.getMessage());
        }
    }
}
