package com.tradingdemo.controller;
import com.tradingdemo.dao.LoginAuditDAO;
import com.tradingdemo.service.AuthService;
import com.tradingdemo.util.AlertUtils;
import com.tradingdemo.util.QRCodeUtil;
import com.tradingdemo.util.TOTPUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Pos;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.regex.Pattern;

public class ProfilController {
    @FXML
    private TextField emailField;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private Button resetButton;

    @FXML
    private TextField phoneField;

    @FXML
    private Button registerButton;

    @FXML
    private Button backButton;
    @FXML
    private Label twoFactorStatusLabel;
    @FXML
    private Button enable2FAButton;
    @FXML
    private Button disable2FAButton;
    @FXML
    private Label ipAddressLabel;
    @FXML
    private Label locationLabel;
    @FXML
    private Label ispLabel;
    @FXML
    private Label timezoneLabel;
    @FXML
    private javafx.scene.control.ListView<String> loginHistoryListView;
    private final AuthService authService = new AuthService();
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";
    public void initialize() {
        backButton.setOnAction(e -> goBack());
        registerButton.setOnAction(event -> updateProfile());
        resetButton.setOnAction(event -> resetForm());
        enable2FAButton.setOnAction(e -> enableTwoFactor());
        disable2FAButton.setOnAction(e -> disableTwoFactor());
        loadUserData();
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

    @FXML
    private void updateProfile() {
        // Get input values
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        // Validation
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() ||
                phone.isEmpty() ) {
            AlertUtils.showWarning("Input Error", "All fields are required");
            return;
        }

        if (!Pattern.matches(EMAIL_PATTERN, email)) {
            AlertUtils.showWarning("Input Error", "Please enter a valid email address");
            return;
        }

        try {
            // Attempt registration
            var user = authService.updateProfile(firstName, lastName, email, phone);
            if (user != null) {
                System.out.println("DEBUG: Registration successful, loading dashboard...");
                AlertUtils.showInfo("Success", "Account updated successfully! " + firstName);
                //loadDashboard();
            } else {
                System.out.println("DEBUG: Update failed");
                AlertUtils.showError("Update Failed", "Update Failed");
            }
        } catch (Exception e) {
            System.err.println("ERROR during Update: " + e.getMessage());
            e.printStackTrace();
            AlertUtils.showError("Update Error", "An error occurred: " + e.getMessage());
        }
    }
    private void loadUserData() {
        var currentUser = AuthService.getCurrentUser();

        if (currentUser != null) {
            firstNameField.setText(currentUser.getFirstName());
            lastNameField.setText(currentUser.getLastName());
            emailField.setText(currentUser.getEmail());
            phoneField.setText(currentUser.getPhone());
            if (currentUser.isTwoFactorEnabled()) {
                twoFactorStatusLabel.setText("2FA: Enabled");
                enable2FAButton.setDisable(true);
                disable2FAButton.setDisable(false);
            } else {
                twoFactorStatusLabel.setText("2FA: Not enabled");
                enable2FAButton.setDisable(false);
                disable2FAButton.setDisable(true);
            }
        }

        // Display session IP information
        var ipInfo = AuthService.getSessionIPInfo();
        if (ipInfo != null) {
            ipAddressLabel.setText("IP Address: " + ipInfo.ip);
            locationLabel.setText("Location: " + ipInfo.city + ", " + ipInfo.country);
            ispLabel.setText("ISP: " + ipInfo.isp);
            timezoneLabel.setText("Timezone: " + ipInfo.timezone);
        } else {
            ipAddressLabel.setText("IP Address: N/A");
            locationLabel.setText("Location: N/A");
            ispLabel.setText("ISP: N/A");
            timezoneLabel.setText("Timezone: N/A");
        }

        // Load login history
        if (loginHistoryListView != null && currentUser != null) {
            LoginAuditDAO auditDAO = new LoginAuditDAO();
            var history = auditDAO.getLoginHistory(currentUser.getId(), 20);
            loginHistoryListView.getItems().clear();
            loginHistoryListView.getItems().addAll(history);
        }
    }

    private void enableTwoFactor() {
        var currentUser = AuthService.getCurrentUser();
        if (currentUser == null) {
            AlertUtils.showError("Error", "No user logged in.");
            return;
        }

        try {
            String secret = TOTPUtil.generateSecret();
            String issuer = "TradingWolfers";
            String account = currentUser.getEmail();
            String otpAuth = TOTPUtil.getOtpAuthURL(account, issuer, secret);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tradingdemo/view/twofactor_setup.fxml"));
            Parent root = loader.load();
            TwoFactorController ctrl = loader.getController();
            ctrl.initData(currentUser, secret, otpAuth);

            Stage modal = new Stage();
            modal.initOwner(backButton.getScene().getWindow());
            modal.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            modal.setTitle("Setup Two-Factor Authentication");
            Scene scene = new Scene(root);
            modal.setScene(scene);
            modal.showAndWait();

            // Refresh UI after modal closes
            loadUserData();
        } catch (Exception e) {
            System.err.println("ERROR opening 2FA modal: " + e.getMessage());
            e.printStackTrace();
            AlertUtils.showError("Error", "Could not open 2FA setup: " + e.getMessage());
        }
    }

    private void disableTwoFactor() {
        var currentUser = AuthService.getCurrentUser();
        if (currentUser == null) {
            AlertUtils.showError("Error", "No user logged in.");
            return;
        }

        // Prompt for current TOTP code to confirm disabling
        javafx.scene.control.TextInputDialog tid = new javafx.scene.control.TextInputDialog();
        tid.setTitle("Disable Two-Factor Authentication");
        tid.setHeaderText("Enter current 6-digit authentication code to disable 2FA");
        tid.setContentText("Code:");
        Optional<String> res = tid.showAndWait();
        if (res.isPresent()) {
            try {
                int code = Integer.parseInt(res.get().trim());
                boolean ok = authService.verifyTwoFactor(currentUser, code);
                if (ok) {
                    boolean updated = authService.disableTwoFactorForUser(currentUser);
                    if (updated) {
                        AlertUtils.showInfo("2FA Disabled", "Two-factor authentication has been disabled.");
                        loadUserData();
                    } else {
                        AlertUtils.showError("Error", "Failed to update 2FA settings.");
                    }
                } else {
                    AlertUtils.showError("2FA Failed", "Invalid authentication code.");
                }
            } catch (NumberFormatException nfe) {
                AlertUtils.showError("Input Error", "Code must be numeric");
            }
        }
    }
    @FXML
    private void resetForm() {

        var currentUser = AuthService.getCurrentUser();

        if (currentUser != null) {
            firstNameField.setText(currentUser.getFirstName());
            lastNameField.setText(currentUser.getLastName());
            emailField.setText(currentUser.getEmail());
            phoneField.setText(currentUser.getPhone());

            AlertUtils.showInfo("Reset", "Form restored successfully.");
        } else {
            AlertUtils.showError("Error", "No user is currently logged in.");
        }
    }
}
