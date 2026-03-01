package com.tradingdemo.controller;

import com.tradingdemo.service.AuthService;
import com.tradingdemo.util.AlertUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class ResetConfirmController {
    @FXML private TextField emailField;
    @FXML private TextField codeField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button resetBtn;
    @FXML private Button backBtn;

    private final AuthService authService = new AuthService();

    public void initialize() {
        resetBtn.setOnAction(e -> doReset());
        backBtn.setOnAction(e -> goBack());
    }

    private void doReset() {
        String email = emailField.getText().trim();
        String code = codeField.getText().trim();
        String pw = newPasswordField.getText();
        String conf = confirmPasswordField.getText();
        if (email.isEmpty() || code.isEmpty() || pw.isEmpty()) { AlertUtils.showWarning("Input Error", "Please complete all fields."); return; }
        if (!pw.equals(conf)) { AlertUtils.showWarning("Input Error", "Passwords do not match."); return; }
        boolean ok = authService.resetPasswordWithCode(email, code, pw);
        if (ok) {
            AlertUtils.showInfo("Success", "Password reset. You may now login.");
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tradingdemo/view/login.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) resetBtn.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
            } catch (IOException ex) {
                AlertUtils.showError("Navigation Error", "Could not load login: " + ex.getMessage());
            }
        } else {
            AlertUtils.showError("Reset Failed", "Invalid code or code expired.");
        }
    }

    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tradingdemo/view/reset_request.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backBtn.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (IOException e) {
            AlertUtils.showError("Navigation Error", "Could not load request screen: " + e.getMessage());
        }
    }
}
