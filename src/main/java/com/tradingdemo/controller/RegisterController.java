package com.tradingdemo.controller;

import java.io.IOException;
import java.util.regex.Pattern;

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
                System.out.println("DEBUG: Registration successful, loading dashboard...");
                AlertUtils.showInfo("Success", "Account created successfully! Welcome " + firstName);
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
