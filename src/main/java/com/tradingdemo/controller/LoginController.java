package com.tradingdemo.controller;

import java.io.IOException;

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

/**
 * LoginController - Handles user login functionality
 * Validates credentials and transitions to dashboard on successful login
 */
public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;

    private final AuthService authService = new AuthService();

    /**
     * Initializes the controller
     */
    @FXML
    public void initialize() {
        loginButton.setOnAction(event -> handleLogin());
        registerButton.setOnAction(event -> handleRegisterNavigation());
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
                System.out.println("DEBUG: Login successful, loading dashboard...");
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
            AlertUtils.showError("Navigation Error", "Could not load dashboard: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("ERROR in dashboard initialization: " + e.getMessage());
            e.printStackTrace();
            AlertUtils.showError("Dashboard Error", "Error initializing dashboard: " + e.getMessage());
        }
    }
}
