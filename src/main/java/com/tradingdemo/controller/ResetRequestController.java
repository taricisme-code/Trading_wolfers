package com.tradingdemo.controller;

import com.tradingdemo.service.AuthService;
import com.tradingdemo.util.AlertUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class ResetRequestController {
    @FXML private TextField emailField;
    @FXML private Button sendCodeBtn;
    @FXML private Button backBtn;

    private final AuthService authService = new AuthService();

    public void initialize() {
        sendCodeBtn.setOnAction(e -> sendCode());
        backBtn.setOnAction(e -> goBack());
    }

    private void sendCode() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) { AlertUtils.showWarning("Input Error", "Please enter your email."); return; }
        boolean ok = authService.initiatePasswordReset(email);
        if (ok) {
            AlertUtils.showInfo("Sent", "A reset code was sent to your email (check spam).\nThen use the 'Enter Reset Code' screen to complete.");
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tradingdemo/view/reset_confirm.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) sendCodeBtn.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
            } catch (IOException ex) {
                AlertUtils.showError("Navigation Error", "Could not open confirm screen: " + ex.getMessage());
            }
        } else {
            AlertUtils.showError("Error", "Could not initiate password reset for that email.");
        }
    }

    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tradingdemo/view/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backBtn.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (IOException e) {
            AlertUtils.showError("Navigation Error", "Could not load login: " + e.getMessage());
        }
    }
}
