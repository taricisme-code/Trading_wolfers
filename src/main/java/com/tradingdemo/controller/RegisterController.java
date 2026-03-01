package com.tradingdemo.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.regex.Pattern;

import com.tradingdemo.service.AuthService;
import com.tradingdemo.util.AlertUtils;
import com.tradingdemo.util.QRCodeUtil;
import com.tradingdemo.util.TOTPUtil;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Pos;

/**
 * RegisterController - Handles user registration functionality
 */
public class RegisterController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button registerButton;
    @FXML private Button loginButton;

    private final AuthService authService = new AuthService();
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";

    @FXML
    public void initialize() {
        registerButton.setOnAction(event -> handleRegister());
        loginButton.setOnAction(event -> handleLoginNavigation());
    }

    @FXML
    private void handleRegister() {
        // Get input values
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validation
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || 
            phone.isEmpty() || password.isEmpty()) {
            AlertUtils.showWarning("Input Error", "All fields are required");
            return;
        }

        if (!Pattern.matches(EMAIL_PATTERN, email)) {
            AlertUtils.showWarning("Input Error", "Please enter a valid email address");
            return;
        }

        if (password.length() < 6) {
            AlertUtils.showWarning("Input Error", "Password must be at least 6 characters");
            return;
        }

        if (!password.equals(confirmPassword)) {
            AlertUtils.showWarning("Input Error", "Passwords do not match");
            return;
        }

        System.out.println("DEBUG: Attempting registration for: " + email);

        try {
            // Attempt registration
            var user = authService.register(firstName, lastName, email, phone, password);
            if (user != null) {
                System.out.println("DEBUG: Registration successful for: " + user.getEmail());
                // Start 2FA setup (generate secret + QR + verify)
                try {
                    boolean enabled = showTwoFactorSetup(user);
                    if (enabled) {
                        AlertUtils.showInfo("2FA Enabled", "Two-factor authentication has been enabled for your account.");
                    } else {
                        AlertUtils.showInfo("Registration Complete", "Account created. You can enable 2FA later in your profile.");
                    }
                } catch (Exception e) {
                    System.err.println("ERROR during 2FA setup: " + e.getMessage());
                }

                loadDashboard();
            } else {
                System.out.println("DEBUG: Registration failed");
                AlertUtils.showError("Registration Failed", "Email already exists or registration failed");
            }
        } catch (Exception e) {
            System.err.println("ERROR during registration: " + e.getMessage());
            e.printStackTrace();
            AlertUtils.showError("Registration Error", "An error occurred: " + e.getMessage());
        }
    }

    /**
     * Show a dialog with QR code and prompt user to enter TOTP code to enable 2FA.
     * Returns true if 2FA was enabled.
     */
    private boolean showTwoFactorSetup(com.tradingdemo.model.User user) throws Exception {
        String secret = TOTPUtil.generateSecret();
        String issuer = "TradingWolfers";
        String account = user.getEmail();
        String otpAuth = TOTPUtil.getOtpAuthURL(account, issuer, secret);

        // Generate QR file
        File qrFile = File.createTempFile("qrcode_", ".png");
        qrFile.deleteOnExit();
        QRCodeUtil.generateQRCodeImage(otpAuth, 250, 250, qrFile.getAbsolutePath());

        // Build dialog with image and input
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Setup Two-Factor Authentication");
        dialog.setHeaderText("Scan the QR with your authenticator app and enter the 6-digit code below");

        ButtonType enableButtonType = new ButtonType("Enable", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(enableButtonType, ButtonType.CANCEL);

        ImageView imageView = new ImageView();
        try (FileInputStream fis = new FileInputStream(qrFile)) {
            Image image = new Image(fis);
            imageView.setImage(image);
            imageView.setFitWidth(200);
            imageView.setFitHeight(200);
            imageView.setPreserveRatio(true);
        }

        TextField codeField = new TextField();
        codeField.setPromptText("123456");

        VBox content = new VBox(10, imageView, new Label("Authentication code:"), codeField);
        content.setAlignment(Pos.CENTER);
        dialog.getDialogPane().setContent(content);

        // Convert result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == enableButtonType) {
                return codeField.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String codeStr = result.get().trim();
            try {
                int code = Integer.parseInt(codeStr);
                boolean ok = TOTPUtil.verifyCode(secret, code);
                if (ok) {
                    boolean updated = authService.enableTwoFactorForUser(user, secret);
                    if (!updated) {
                        System.err.println("Failed to persist 2FA settings for user: " + user.getEmail());
                    }
                    return updated;
                } else {
                    AlertUtils.showError("2FA Failed", "Invalid authentication code. 2FA not enabled.");
                }
            } catch (NumberFormatException nfe) {
                AlertUtils.showError("Input Error", "Code must be numeric");
            }
        }
        return false;
    }

    @FXML
    private void handleLoginNavigation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tradingdemo/view/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (IOException e) {
            AlertUtils.showError("Navigation Error", "Could not load login screen");
        }
    }

    private void loadDashboard() {
        try {
            System.out.println("DEBUG: Loading dashboard FXML...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tradingdemo/view/dashboard.fxml"));
            System.out.println("DEBUG: Dashboard FXML resource: " + getClass().getResource("/com/tradingdemo/view/dashboard.fxml"));
            
            Parent root = loader.load();
            System.out.println("DEBUG: Dashboard FXML loaded successfully");
            
            Stage stage = (Stage) registerButton.getScene().getWindow();
            Scene scene = new Scene(root);
            
            // Apply stylesheet
            String css = getClass().getResource("/com/tradingdemo/view/styles.css").toExternalForm();
            scene.getStylesheets().add(css);
            System.out.println("DEBUG: Stylesheet applied: " + css);
            
            stage.setScene(scene);
            
            System.out.println("DEBUG: Dashboard scene displayed");
        } catch (IOException e) {
            System.err.println("ERROR loading dashboard: " + e.getMessage());
            e.printStackTrace();
            AlertUtils.showError("Navigation Error", "Could not load dashboard: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("ERROR in dashboard initialization: " + e.getMessage());
            e.printStackTrace();
            AlertUtils.showError("Dashboard Error", "Error initializing dashboard: " + e.getMessage());
        }
    }
}
