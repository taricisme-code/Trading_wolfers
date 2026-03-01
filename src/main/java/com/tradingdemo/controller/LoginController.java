package com.tradingdemo.controller;

import java.io.IOException;

import com.tradingdemo.dao.LoginAuditDAO;
import com.tradingdemo.service.AuthService;
import com.tradingdemo.service.IPInfoService;
import com.tradingdemo.util.AlertUtils;
import javafx.scene.control.TextInputDialog;
import java.util.Optional;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * LoginController - Handles user login functionality
 * Validates credentials and transitions to dashboard on successful login
 */
public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Button forgotBtn;

    private final AuthService authService = new AuthService();

    /**
     * Initializes the controller
     */
    @FXML
    public void initialize() {
        loginButton.setOnAction(event -> handleLogin());
        registerButton.setOnAction(event -> handleRegisterNavigation());
        if (forgotBtn != null) forgotBtn.setOnAction(e -> openResetRequest());
    }

    /**
     * Handles login button action
     */
    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Validation
        if (email.isEmpty()) {
            AlertUtils.showWarning("Input Error", "Please enter your email address");
            return;
        }

        if (password.isEmpty()) {
            AlertUtils.showWarning("Input Error", "Please enter your password");
            return;
        }

        System.out.println("DEBUG: Attempting login with email: " + email);

        try {
            // Attempt login
            var user = authService.login(email, password);
            if (user != null) {
                // If user has 2FA enabled, prompt for TOTP code
                if (user.isTwoFactorEnabled()) {
                    TextInputDialog dialog = new TextInputDialog();
                    dialog.setTitle("Two-Factor Authentication");
                    dialog.setHeaderText("Enter the 6-digit authentication code");
                    dialog.setContentText("Code:");

                    Optional<String> result = dialog.showAndWait();
                    if (result.isPresent()) {
                        try {
                            int code = Integer.parseInt(result.get().trim());
                            boolean ok = authService.verifyTwoFactor(user, code);
                            if (!ok) {
                                AlertUtils.showError("2FA Failed", "Invalid authentication code");
                                passwordField.clear();
                                return;
                            }
                        } catch (NumberFormatException e) {
                            AlertUtils.showError("Input Error", "Code must be numeric");
                            passwordField.clear();
                            return;
                        }
                    } else {
                        // Dialog cancelled
                        return;
                    }
                }

                System.out.println("DEBUG: Login successful, loading dashboard...");
                
                // Capture IP information for audit logging and session
                new Thread(() -> {
                    IPInfoService.IPInfo ipInfo = IPInfoService.getIPInfo();
                    // Store in auth service for display in profile
                    com.tradingdemo.service.AuthService.setSessionIPInfo(ipInfo);
                    
                    // Record login in database
                    LoginAuditDAO auditDAO = new LoginAuditDAO();
                    auditDAO.recordLogin(user.getId(), ipInfo);
                    
                    String location = (ipInfo != null) ? ipInfo.city + ", " + ipInfo.country : "Unknown";
                    String ipAddress = (ipInfo != null) ? ipInfo.ip : "N/A";
                    System.out.println("LOGIN AUDIT: User " + user.getEmail() + " logged in from " + location + " (IP: " + ipAddress + ")");
                }).start();
                
                AlertUtils.showInfo("Success", "Welcome " + user.getFirstName() + "!");
                loadDashboard();
            } else {
                System.out.println("DEBUG: Login failed - invalid credentials");
                AlertUtils.showError("Login Failed", "Invalid email or password");
                passwordField.clear();
            }
        } catch (Exception e) {
            System.err.println("ERROR during login: " + e.getMessage());
            e.printStackTrace();
            AlertUtils.showError("Login Error", "An error occurred: " + e.getMessage());
            passwordField.clear();
        }
    }

    private void openResetRequest() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tradingdemo/view/reset_request.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (IOException e) {
            AlertUtils.showError("Navigation Error", "Could not load reset request screen: " + e.getMessage());
        }
    }

    /**
     * Navigates to registration screen
     */
    @FXML
    private void handleRegisterNavigation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tradingdemo/view/register.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) registerButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (IOException e) {
            AlertUtils.showError("Navigation Error", "Could not load registration screen");
        }
    }

    /**
     * Loads the dashboard screen after successful login
     */
    private void loadDashboard() {
        try {
            System.out.println("DEBUG: Loading dashboard FXML...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tradingdemo/view/dashboard.fxml"));
            System.out.println("DEBUG: Dashboard FXML resource: " + getClass().getResource("/com/tradingdemo/view/dashboard.fxml"));
            
            Parent root = loader.load();
            System.out.println("DEBUG: Dashboard FXML loaded successfully");
            
            Stage stage = (Stage) loginButton.getScene().getWindow();
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
            // persist stacktrace to file for debugging
            try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter("dashboard-load-error.log", true))) {
                e.printStackTrace(pw);
            } catch (Exception ex) {
                System.err.println("Failed to write dashboard-load-error.log: " + ex.getMessage());
            }
            AlertUtils.showError("Navigation Error", "Could not load dashboard: " + e.getMessage() + " (see dashboard-load-error.log)");
        } catch (Exception e) {
            System.err.println("ERROR in dashboard initialization: " + e.getMessage());
            e.printStackTrace();
            try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter("dashboard-load-error.log", true))) {
                e.printStackTrace(pw);
            } catch (Exception ex) {
                System.err.println("Failed to write dashboard-load-error.log: " + ex.getMessage());
            }
            AlertUtils.showError("Dashboard Error", "Error initializing dashboard: " + e.getMessage() + " (see dashboard-load-error.log)");
        }
    }
}
